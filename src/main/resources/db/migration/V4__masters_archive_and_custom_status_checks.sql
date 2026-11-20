-- MUSEO v4: archive retired masters, add the active roster and supplied CSV
-- catalog, and constrain custom-request/design states. Existing historical
-- rows and their relationships are preserved.

-- Fail closed if the existing dataset differs from the verified V1 snapshot,
-- if a new master already exists, or if existing states would violate CHECKs.
DO $$
DECLARE
  v_invalid_request_statuses text[];
  v_invalid_design_statuses text[];
  v_expected record;
BEGIN
  IF (SELECT count(*) FROM museo.masters) <> 6 THEN
    RAISE EXCEPTION 'V4 precondition failed: expected the six verified legacy masters; found %',
      (SELECT count(*) FROM museo.masters);
  END IF;

  FOR v_expected IN
    SELECT * FROM (VALUES
      (1::bigint, 'Richard Valderrama'::text),
      (2::bigint, 'José Naria'::text),
      (3::bigint, 'María Cabrera'::text),
      (4::bigint, 'Ricardo Hernaza'::text),
      (5::bigint, 'David Uzcátegui'::text),
      (6::bigint, 'Wilson Chávez'::text)
    ) AS legacy(master_id, name)
  LOOP
    IF NOT EXISTS (
      SELECT 1 FROM museo.masters m
      WHERE m.master_id = v_expected.master_id AND m.name = v_expected.name
    ) THEN
      RAISE EXCEPTION 'V4 precondition failed: legacy master ID % is not %',
        v_expected.master_id, v_expected.name;
    END IF;
  END LOOP;

  IF EXISTS (
    SELECT 1 FROM museo.masters
    WHERE name IN ('Gilberto Granja','Guillermo Lasso','José Castrillón','Jesús Caguasongo')
  ) THEN
    RAISE EXCEPTION 'V4 precondition failed: one or more new master names already exist';
  END IF;

  SELECT array_agg(DISTINCT COALESCE(status, '<NULL>') ORDER BY COALESCE(status, '<NULL>')) INTO v_invalid_request_statuses
  FROM museo.custom_requests
  WHERE status IS NULL OR NOT (status = ANY (ARRAY[
    'borrador','abierta','aceptada','en_produccion','terminada','completada','cancelada','vencida'
  ]::text[]));
  IF v_invalid_request_statuses IS NOT NULL THEN
    RAISE EXCEPTION 'V4 stopped: incompatible custom_requests.status values: %',
      array_to_string(v_invalid_request_statuses, ', ');
  END IF;

  SELECT array_agg(DISTINCT COALESCE(status, '<NULL>') ORDER BY COALESCE(status, '<NULL>')) INTO v_invalid_design_statuses
  FROM museo.custom_request_designs
  WHERE status IS NULL OR NOT (status = ANY (ARRAY['pendiente','completado','fallido']::text[]));
  IF v_invalid_design_statuses IS NOT NULL THEN
    RAISE EXCEPTION 'V4 stopped: incompatible custom_request_designs.status values: %',
      array_to_string(v_invalid_design_statuses, ', ');
  END IF;
END $$;

ALTER TABLE museo.masters
  ADD COLUMN is_active boolean NOT NULL DEFAULT true;

-- IDs come from the verified V1 backup. Do not rename legacy masters or alter
-- their profiles, stores, artwork, auctions, bids, or audit history.
UPDATE museo.masters SET is_active = true WHERE master_id IN (1,3);
UPDATE museo.masters SET is_active = false WHERE master_id IN (2,4,5,6);

-- Official additions have no unverified biography, workshop, contact, user,
-- or store data. Draft store creation remains possible after account linking.
INSERT INTO museo.masters
  (name, user_id, biography_es, biography_en, workshop, contact_info,
   verification_status, is_active)
VALUES
  ('Gilberto Granja', NULL, NULL, NULL, 'GRANJA', NULL, 'pendiente', true),
  ('Guillermo Lasso', NULL, NULL, NULL, 'Lassos de Arte', NULL, 'pendiente', true),
  ('José Castrillón', NULL, NULL, NULL, 'Artesanias Castrillón', NULL, 'pendiente', true),
  ('Jesús Caguasongo', NULL, NULL, NULL, 'Jesus Ceballos', NULL, 'pendiente', true);

ALTER TABLE museo.custom_requests
  ADD CONSTRAINT custom_requests_status_check
  CHECK (status IS NOT NULL AND status IN ('borrador','abierta','aceptada','en_produccion','terminada','completada','cancelada','vencida'));

ALTER TABLE museo.custom_request_designs
  ADD CONSTRAINT custom_request_designs_status_check
  CHECK (status IS NOT NULL AND status IN ('pendiente','completado','fallido'));

-- Keep the existing public catalog interface, while hiding archived masters'
-- work from new browsing/operations. Historical sales remain in v_store_sales.
CREATE OR REPLACE VIEW museo.v_artwork_catalog AS
SELECT a.artwork_id, a.slug, a.title_es, a.title_en, a.history_es, a.history_en,
       a.technique, a.width_cm, a.height_cm, a.depth_cm, a.weight_g, a.sale_mode,
       a.price, a.currency, a.stock, a.status AS artwork_status,
       s.store_id, s.slug AS store_slug, s.name_es AS store_name_es, s.name_en AS store_name_en,
       m.master_id, m.name AS master_name,
       COALESCE((SELECT jsonb_agg(jsonb_build_object('photo_id',p.photo_id,'storage_key',p.storage_key,'file_url',p.file_url,'alt_es',p.alt_es,'alt_en',p.alt_en)
                    ORDER BY p.sort_order) FROM museo.photos p WHERE p.artwork_id=a.artwork_id), '[]'::jsonb) AS photos,
       COALESCE((SELECT jsonb_agg(c.code ORDER BY c.sort_order) FROM museo.artwork_colors ac JOIN museo.colors c USING(color_id)
                    WHERE ac.artwork_id=a.artwork_id AND c.is_active), '[]'::jsonb) AS color_codes,
       COALESCE((SELECT jsonb_agg(mt.code ORDER BY mt.sort_order) FROM museo.artwork_motifs am JOIN museo.motifs mt USING(motif_id)
                    WHERE am.artwork_id=a.artwork_id AND mt.is_active), '[]'::jsonb) AS motif_codes
FROM museo.artworks a
JOIN museo.stores s USING(store_id)
JOIN museo.masters m USING(master_id)
WHERE a.deleted_at IS NULL AND a.status IN ('exhibida','subasta')
  AND s.status='publicada' AND m.verification_status='aprobado' AND m.is_active;

-- Preserve V2's publication approval rule and also disallow publishing a
-- store owned by an archived master. Draft stores remain available to active,
-- not-yet-approved masters during onboarding.
CREATE OR REPLACE FUNCTION museo.enforce_store_publication_approval()
RETURNS trigger LANGUAGE plpgsql SET search_path = museo, pg_catalog AS $$
DECLARE v_status text; v_active boolean;
BEGIN
  IF NEW.status = 'publicada'
     AND (TG_OP = 'INSERT' OR OLD.status IS DISTINCT FROM NEW.status OR OLD.master_id IS DISTINCT FROM NEW.master_id) THEN
    SELECT verification_status, is_active INTO v_status, v_active
    FROM museo.masters WHERE master_id = NEW.master_id FOR UPDATE;
    IF v_active IS DISTINCT FROM true THEN
      RAISE EXCEPTION 'Una tienda solo puede publicarse para un maestro activo';
    END IF;
    IF v_status IS DISTINCT FROM 'aprobado' THEN
      RAISE EXCEPTION 'La tienda solo puede publicarse cuando el maestro está aprobado';
    END IF;
  END IF;
  RETURN NEW;
END $$;

-- Block new operational records for inactive masters without deleting or
-- rewriting any existing operational/history rows.
CREATE OR REPLACE FUNCTION museo.require_active_master_for_new_operations()
RETURNS trigger LANGUAGE plpgsql SET search_path = museo, pg_catalog AS $$
DECLARE v_master_id bigint; v_active boolean;
BEGIN
  IF TG_OP = 'UPDATE' THEN
    CASE TG_TABLE_NAME
      WHEN 'stores' THEN
        IF NEW.master_id IS NOT DISTINCT FROM OLD.master_id THEN RETURN NEW; END IF;
      WHEN 'artworks' THEN
        IF NEW.store_id IS NOT DISTINCT FROM OLD.store_id THEN RETURN NEW; END IF;
      WHEN 'photos' THEN
        IF NEW.artwork_id IS NOT DISTINCT FROM OLD.artwork_id THEN RETURN NEW; END IF;
      WHEN 'auctions' THEN
        IF NEW.artwork_id IS NOT DISTINCT FROM OLD.artwork_id THEN RETURN NEW; END IF;
      WHEN 'bids' THEN
        IF NEW.auction_id IS NOT DISTINCT FROM OLD.auction_id
           AND NEW.amount IS NOT DISTINCT FROM OLD.amount THEN RETURN NEW; END IF;
      WHEN 'custom_offers' THEN
        IF NEW.master_id IS NOT DISTINCT FROM OLD.master_id
           AND NEW.store_id IS NOT DISTINCT FROM OLD.store_id THEN RETURN NEW; END IF;
      WHEN 'order_items' THEN
        IF NEW.store_id IS NOT DISTINCT FROM OLD.store_id THEN RETURN NEW; END IF;
      WHEN 'custom_requests' THEN
        IF NEW.target_master_id IS NOT DISTINCT FROM OLD.target_master_id THEN RETURN NEW; END IF;
      WHEN 'conversations' THEN
        IF NEW.master_id IS NOT DISTINCT FROM OLD.master_id THEN RETURN NEW; END IF;
    END CASE;
  END IF;

  CASE TG_TABLE_NAME
    WHEN 'stores' THEN v_master_id := NEW.master_id;
    WHEN 'artworks' THEN
      SELECT master_id INTO v_master_id FROM museo.stores WHERE store_id=NEW.store_id;
    WHEN 'photos' THEN
      SELECT s.master_id INTO v_master_id
      FROM museo.artworks a JOIN museo.stores s USING(store_id)
      WHERE a.artwork_id=NEW.artwork_id;
    WHEN 'custom_offers' THEN v_master_id := NEW.master_id;
    WHEN 'custom_requests' THEN v_master_id := NEW.target_master_id;
    WHEN 'conversations' THEN v_master_id := NEW.master_id;
    WHEN 'order_items' THEN
      SELECT master_id INTO v_master_id FROM museo.stores WHERE store_id=NEW.store_id;
    WHEN 'auctions' THEN
      SELECT s.master_id INTO v_master_id
      FROM museo.artworks a JOIN museo.stores s USING(store_id)
      WHERE a.artwork_id=NEW.artwork_id;
    WHEN 'bids' THEN
      SELECT s.master_id INTO v_master_id
      FROM museo.auctions u JOIN museo.artworks a USING(artwork_id)
      JOIN museo.stores s USING(store_id)
      WHERE u.auction_id=NEW.auction_id;
  END CASE;

  IF v_master_id IS NULL THEN RETURN NEW; END IF;
  SELECT is_active INTO v_active FROM museo.masters WHERE master_id=v_master_id FOR SHARE;
  IF v_active IS DISTINCT FROM true THEN
    RAISE EXCEPTION 'No se permiten nuevas operaciones para maestros archivados';
  END IF;
  RETURN NEW;
END $$;

CREATE TRIGGER stores_require_active_master
  BEFORE INSERT OR UPDATE OF master_id ON museo.stores
  FOR EACH ROW EXECUTE FUNCTION museo.require_active_master_for_new_operations();
CREATE TRIGGER artworks_require_active_master
  BEFORE INSERT OR UPDATE OF store_id ON museo.artworks
  FOR EACH ROW EXECUTE FUNCTION museo.require_active_master_for_new_operations();
CREATE TRIGGER photos_require_active_master
  BEFORE INSERT OR UPDATE OF artwork_id ON museo.photos
  FOR EACH ROW EXECUTE FUNCTION museo.require_active_master_for_new_operations();
CREATE TRIGGER auctions_require_active_master
  BEFORE INSERT OR UPDATE OF artwork_id ON museo.auctions
  FOR EACH ROW EXECUTE FUNCTION museo.require_active_master_for_new_operations();
CREATE TRIGGER bids_require_active_master
  BEFORE INSERT OR UPDATE OF auction_id, amount ON museo.bids
  FOR EACH ROW EXECUTE FUNCTION museo.require_active_master_for_new_operations();
CREATE TRIGGER custom_offers_require_active_master
  BEFORE INSERT OR UPDATE OF master_id, store_id ON museo.custom_offers
  FOR EACH ROW EXECUTE FUNCTION museo.require_active_master_for_new_operations();
CREATE TRIGGER order_items_require_active_master
  BEFORE INSERT OR UPDATE OF store_id ON museo.order_items
  FOR EACH ROW EXECUTE FUNCTION museo.require_active_master_for_new_operations();
CREATE TRIGGER custom_requests_require_active_target_master
  BEFORE INSERT OR UPDATE OF target_master_id ON museo.custom_requests
  FOR EACH ROW EXECUTE FUNCTION museo.require_active_master_for_new_operations();
CREATE TRIGGER conversations_require_active_master
  BEFORE INSERT OR UPDATE OF master_id ON museo.conversations
  FOR EACH ROW EXECUTE FUNCTION museo.require_active_master_for_new_operations();

-- The exact 21 work rows and 32 image paths below come only from the CSV
-- supplied for this migration. English titles are absent in that source, so
-- title_en preserves the supplied title; prices and currencies remain NULL.
-- New catalog values preserve source terms that V2 does not yet catalog.
INSERT INTO museo.shapes(code,name_es,name_en,sort_order)
VALUES ('banco','Banco','Bench',13)
ON CONFLICT (code) DO NOTHING;

INSERT INTO museo.motifs(code,name_es,name_en,sort_order)
VALUES ('antropomorfo','Antropomorfo','Anthropomorphic',9),
       ('paisaje','Paisaje','Landscape',10)
ON CONFLICT (code) DO NOTHING;

CREATE TEMP TABLE v4_artwork_seed (
  master_name text NOT NULL,
  workshop text NOT NULL,
  title_es text NOT NULL,
  description_es text NOT NULL,
  description_en text NOT NULL,
  shape_code text NOT NULL,
  color_codes text[] NOT NULL,
  motif_codes text[] NOT NULL,
  style_code text NOT NULL,
  width_cm numeric(8,2),
  height_cm numeric(8,2),
  depth_cm numeric(8,2),
  stock integer NOT NULL,
  UNIQUE(master_name,title_es)
) ON COMMIT DROP;

INSERT INTO v4_artwork_seed VALUES
('Gilberto Granja','GRANJA','Bargueño Colonial','Bargueño de estilo colonial con tapa abatible, gavetas y compartimentos cubiertos de flores y grecas.','Colonial-style writing cabinet with a drop-front lid, drawers and compartments covered in flowers and frets.','mueble',ARRAY['blanco','verde','rosado','morado','dorado','cafe'],ARRAY['floral','geometrico'],'tradicional',NULL,NULL,NULL,1),
('Gilberto Granja','GRANJA','Bargueño','Bargueño de cuatro gavetas cubierto de flores, con un medallón de paisaje andino con casas y montañas.','Four-drawer cabinet covered in flowers, with a medallion of an Andean landscape with houses and mountains.','mueble',ARRAY['cafe','verde','azul','rosado','naranja'],ARRAY['floral','paisaje'],'tradicional',NULL,NULL,NULL,1),
('Gilberto Granja','GRANJA','Jarron Flora Azul','Jarrón alto de boca acampanada con rosas estilizadas en azul, turquesa y crema.','Tall flared vase with stylized roses in blue, turquoise and cream.','jarron',ARRAY['azul','verde','blanco','morado'],ARRAY['floral'],'contemporaneo',NULL,NULL,NULL,1),
('Gilberto Granja','GRANJA','Jarron Flora Dorada Fondo Blanco','Jarrón ovoide cubierto de flores y hojas doradas sobre fondo blanco.','Ovoid vase covered in golden flowers and leaves on a white background.','jarron',ARRAY['dorado','blanco'],ARRAY['floral'],'contemporaneo',NULL,NULL,NULL,1),
('Gilberto Granja','GRANJA','Jarron Flora Dorada Fondo Negro','Jarrón ovoide cubierto de flores y hojas doradas sobre fondo negro.','Ovoid vase covered in golden flowers and leaves on a black background.','jarron',ARRAY['dorado','negro'],ARRAY['floral'],'contemporaneo',NULL,NULL,NULL,1),
('Guillermo Lasso','Lassos de Arte','Cuadro Tradición','Cuadro tallado en relieve con rostros, una flauta y un cuenco, en colores vivos y patrones geométricos.','Carved relief panel with faces, a flute and a bowl, in bright colors and geometric patterns.','cuadro',ARRAY['multicolor'],ARRAY['antropomorfo','geometrico'],'contemporaneo',NULL,NULL,NULL,1),
('Guillermo Lasso','Lassos de Arte','Máscara con Diseños Precolombinos','Máscara de rostro sereno con tocado y orejeras en espiral, decorada con diseños precolombinos.','Mask with a serene face, headdress and spiral ear ornaments, decorated with pre-Columbian designs.','mascara',ARRAY['verde','rojo','amarillo','azul','rosado'],ARRAY['precolombino','antropomorfo'],'tradicional',NULL,NULL,NULL,1),
('Jesús Caguasongo','Jesus Ceballos','Sol de los Pastos','Plato con el Sol de los Pastos de ocho puntas, espirales y rostros, rodeado de grecas.','Plate with the eight-pointed Sun of the Pastos, spirals and faces, framed by frets.','plato',ARRAY['naranja','rojo','negro','dorado','azul','blanco'],ARRAY['precolombino','geometrico','antropomorfo'],'fusion',NULL,NULL,NULL,1),
('Jesús Caguasongo','Jesus Ceballos','Toro Talla','Toro tallado en madera en posición de embestida, decorado con filigranas y flores.','Carved wooden bull in a charging pose, decorated with filigree and flowers.','escultura',ARRAY['cafe','amarillo','rojo','verde','dorado'],ARRAY['zoomorfo','floral'],'tradicional',40,20,14,1),
('Jesús Caguasongo','Jesus Ceballos','Galeras','Plato con el volcán Galeras y un pueblo andino al centro, rodeado de flores.','Plate with the Galeras volcano and an Andean village in the center, surrounded by flowers.','plato',ARRAY['cafe','dorado','rojo','verde'],ARRAY['paisaje','floral'],'tradicional',NULL,NULL,NULL,1),
('José Castrillón','Artesanias Castrillón','Cultura y Tradicion','Relieve de figuras con sombreros, ponchos y vasijas que celebra las costumbres de Nariño.','Relief of figures with hats, ponchos and pots celebrating the customs of Nariño.','cuadro',ARRAY['multicolor'],ARRAY['antropomorfo','geometrico'],'tradicional',NULL,NULL,NULL,1),
('José Castrillón','Artesanias Castrillón','Familia Papagayo','Escultura de una familia de papagayos sobre una rama, con plumaje dorado y verde.','Sculpture of a family of parrots on a branch, with golden and green plumage.','escultura',ARRAY['dorado','verde','negro','blanco','naranja'],ARRAY['zoomorfo','floral'],'tradicional',33,67,25,1),
('José Castrillón','Artesanias Castrillón','Mascara musico','Máscara alargada de un músico con corona de plumas tocando un tambor.','Elongated mask of a musician with a feathered crown playing a drum.','mascara',ARRAY['dorado','verde','rosado','rojo','blanco'],ARRAY['antropomorfo','floral'],'tradicional',NULL,NULL,NULL,1),
('María Cabrera','Cabrera','Mensajero Ancestral','Banco con un retrato indígena en el asiento, un jaguar y un colibrí en los costados y patas curvas con figuras precolombinas.','Stool with an indigenous portrait on the seat, a jaguar and a hummingbird on the sides and curved legs with pre-Columbian figures.','banco',ARRAY['azul','verde','rojo','naranja','rosado','negro'],ARRAY['antropomorfo','zoomorfo','precolombino'],'contemporaneo',NULL,NULL,NULL,1),
('María Cabrera','Cabrera','Sabiduria Ancestral','Banco con el retrato de una abuela indígena en el asiento, sabedores en los costados y patas curvas con figuras precolombinas.','Stool with the portrait of an indigenous elder on the seat, wise people on the sides and curved legs with pre-Columbian figures.','banco',ARRAY['azul','naranja','rosado','verde','rojo','negro'],ARRAY['antropomorfo','precolombino'],'contemporaneo',NULL,NULL,NULL,1),
('María Cabrera','Cabrera','Tradición','Banco con un rostro ancestral en el asiento, un taita con tocado de plumas y una guacamaya en los costados.','Stool with an ancestral face on the seat, an elder with a feathered headdress and a macaw on the sides.','banco',ARRAY['naranja','negro','verde','rojo','amarillo','azul'],ARRAY['antropomorfo','zoomorfo','precolombino'],'contemporaneo',NULL,NULL,NULL,1),
('Richard Valderrama','Valderrama','Cuadro Mujer y Tradicion','Cuadro de una mujer envuelta en aves y tejidos geométricos sobre fondo dorado.','Panel of a woman wrapped in birds and geometric weavings on a golden background.','cuadro',ARRAY['dorado','verde','cafe','rojo','blanco'],ARRAY['antropomorfo','zoomorfo','geometrico'],'contemporaneo',NULL,NULL,NULL,1),
('Richard Valderrama','Valderrama','Jarron Clasico','Jarrón con tapa en rojo, decorado con aves estilizadas de inspiración precolombina en blanco y amarillo.','Lidded red vase decorated with stylized pre-Columbian-inspired birds in white and yellow.','jarron',ARRAY['rojo','blanco','amarillo','negro'],ARRAY['precolombino','zoomorfo'],'tradicional',NULL,NULL,NULL,1),
('Richard Valderrama','Valderrama','Jarron Colibri','Jarrón de madera natural con dos franjas de colibríes, cardenales y aves en formas geométricas.','Natural wood vase with two bands of hummingbirds, cardinals and birds in geometric shapes.','jarron',ARRAY['cafe','rojo','amarillo','verde','naranja','blanco'],ARRAY['zoomorfo','geometrico'],'fusion',NULL,NULL,NULL,1),
('Richard Valderrama','Valderrama','Paisaje tipico de Nariño','Cuadro ovalado con un paisaje de Nariño: campesinos, muros de piedra, cultivos y una casa de hacienda.','Oval panel with a Nariño landscape: farmers, stone walls, crops and a country house.','cuadro',ARRAY['verde','cafe','amarillo','rojo','azul'],ARRAY['paisaje','antropomorfo'],'tradicional',46,34,3,1),
('Richard Valderrama','Valderrama','Plato Decorado','Plato negro con una estrella central y grecas, ajedrezados y animales estilizados de inspiración precolombina.','Black plate with a central star and frets, checkerboards and stylized pre-Columbian-inspired animals.','plato',ARRAY['negro','naranja','verde','dorado','azul','blanco'],ARRAY['precolombino','geometrico','zoomorfo'],'tradicional',40,40,NULL,1);

CREATE TEMP TABLE v4_photo_seed (
  master_name text NOT NULL,
  title_es text NOT NULL,
  file_url text NOT NULL,
  sort_order integer NOT NULL,
  is_primary boolean NOT NULL,
  UNIQUE(master_name,title_es,file_url),
  UNIQUE(master_name,title_es,sort_order)
) ON COMMIT DROP;

INSERT INTO v4_photo_seed VALUES
('Gilberto Granja','Bargueño Colonial','/obras/Gilberto Granja/GilbertoGranja - Bargueño Colonial (1).webp',1,true),
('Gilberto Granja','Bargueño Colonial','/obras/Gilberto Granja/GilbertoGranja - Bargueño Colonial (2).webp',2,false),
('Gilberto Granja','Bargueño','/obras/Gilberto Granja/GilbertoGranja - Bargueño.avif',1,true),
('Gilberto Granja','Jarron Flora Azul','/obras/Gilberto Granja/GilbertoGranja - Jarron Flora Azul (1).webp',1,true),
('Gilberto Granja','Jarron Flora Azul','/obras/Gilberto Granja/GilbertoGranja - Jarron Flora Azul (2).webp',2,false),
('Gilberto Granja','Jarron Flora Dorada Fondo Blanco','/obras/Gilberto Granja/GilbertoGranja - Jarron Flora Dorada Fondo Blanco.webp',1,true),
('Gilberto Granja','Jarron Flora Dorada Fondo Negro','/obras/Gilberto Granja/GilbertoGranja - Jarron Flora Dorada Fondo Negro.webp',1,true),
('Guillermo Lasso','Cuadro Tradición','/obras/Guillermo Lasso/GuillermoLasso - Cuadro Tradición.avif',1,true),
('Guillermo Lasso','Máscara con Diseños Precolombinos','/obras/Guillermo Lasso/GuillermoLasso - Máscara con Diseños Precolombinos.avif',1,true),
('Jesús Caguasongo','Sol de los Pastos','/obras/JesusCaguasongo/JesusCaguasango - Sol de los Pastos.avif',1,true),
('Jesús Caguasongo','Toro Talla','/obras/JesusCaguasongo/JesusCaguasongo - Toro Talla - 40cmx20cmx14cm.avif',1,true),
('Jesús Caguasongo','Galeras','/obras/JesusCaguasongo/JesusCaguasongo -- Galeras.avif',1,true),
('José Castrillón','Cultura y Tradicion','/obras/Jose Castrillon/JoseCastrillon - Cultura y Tradicion.avif',1,true),
('José Castrillón','Familia Papagayo','/obras/Jose Castrillon/JoseCastrillon - Familia Papagayo - 67cmx33cmx25cm.avif',1,true),
('José Castrillón','Mascara musico','/obras/Jose Castrillon/JoseCastrillon - Mascara musico.avif',1,true),
('María Cabrera','Mensajero Ancestral','/obras/Maria Cabrera/Maria Cabrera - Mensajero Ancestral (1).png',1,true),
('María Cabrera','Mensajero Ancestral','/obras/Maria Cabrera/Maria Cabrera - Mensajero Ancestral (2).png',2,false),
('María Cabrera','Mensajero Ancestral','/obras/Maria Cabrera/Maria Cabrera - Mensajero Ancestral (3).png',3,false),
('María Cabrera','Mensajero Ancestral','/obras/Maria Cabrera/Maria Cabrera - Mensajero Ancestral (4).png',4,false),
('María Cabrera','Sabiduria Ancestral','/obras/Maria Cabrera/Maria Cabrera - Sabiduria Ancestral (1).png',1,true),
('María Cabrera','Sabiduria Ancestral','/obras/Maria Cabrera/Maria Cabrera - Sabiduria Ancestral (2).png',2,false),
('María Cabrera','Sabiduria Ancestral','/obras/Maria Cabrera/Maria Cabrera - Sabiduria Ancestral (3).png',3,false),
('María Cabrera','Sabiduria Ancestral','/obras/Maria Cabrera/Maria Cabrera - Sabiduria Ancestral (4).png',4,false),
('María Cabrera','Tradición','/obras/Maria Cabrera/Maria Cabrera - Tradición (1).png',1,true),
('María Cabrera','Tradición','/obras/Maria Cabrera/Maria Cabrera - Tradición (2).png',2,false),
('María Cabrera','Tradición','/obras/Maria Cabrera/Maria Cabrera - Tradición (3).png',3,false),
('María Cabrera','Tradición','/obras/Maria Cabrera/Maria Cabrera - Tradición (4).png',4,false),
('Richard Valderrama','Cuadro Mujer y Tradicion','/obras/Richard Valderrama/RichardValderrama - Cuadro Mujer y Tradicion.jpeg',1,true),
('Richard Valderrama','Jarron Clasico','/obras/Richard Valderrama/RichardValderrama - Jarron Clasico.jpeg',1,true),
('Richard Valderrama','Jarron Colibri','/obras/Richard Valderrama/RichardValderrama - Jarron Colibri.jpeg',1,true),
('Richard Valderrama','Paisaje tipico de Nariño','/obras/Richard Valderrama/RichardValderrama - Paisaje tipico de Nariño - 34cmx46cmx3cm.avif',1,true),
('Richard Valderrama','Plato Decorado','/obras/Richard Valderrama/RichardValderrama - Plato Decorado - 40cm de diametro.avif',1,true);

DO $$
BEGIN
  IF (SELECT count(*) FROM v4_artwork_seed) <> 21 OR (SELECT count(*) FROM v4_photo_seed) <> 32 THEN
    RAISE EXCEPTION 'V4 stopped: expected 21 works and 32 photos in the supplied catalog';
  END IF;
  IF EXISTS (
    SELECT 1 FROM v4_artwork_seed x
    LEFT JOIN museo.masters m ON m.name=x.master_name AND m.is_active
    LEFT JOIN museo.shapes sh ON sh.code=x.shape_code AND sh.is_active
    LEFT JOIN museo.styles st ON st.code=x.style_code AND st.is_active
    WHERE m.master_id IS NULL OR sh.shape_id IS NULL OR st.style_id IS NULL
       OR EXISTS (SELECT 1 FROM unnest(x.color_codes) v(code) LEFT JOIN museo.colors c ON c.code=v.code WHERE c.color_id IS NULL OR c.is_active IS DISTINCT FROM true)
       OR EXISTS (SELECT 1 FROM unnest(x.motif_codes) v(code) LEFT JOIN museo.motifs mt ON mt.code=v.code WHERE mt.motif_id IS NULL OR mt.is_active IS DISTINCT FROM true)
  ) THEN
    RAISE EXCEPTION 'V4 stopped: a required master or catalog value (shape, color, motif, style) is missing or inactive; no historical catalog rows were changed';
  END IF;
  IF EXISTS (
    SELECT 1 FROM v4_artwork_seed x
    JOIN museo.masters m ON m.name=x.master_name
    JOIN museo.stores s USING(master_id)
    JOIN museo.artworks a USING(store_id)
    WHERE a.title_es=x.title_es
  ) THEN
    RAISE EXCEPTION 'V4 stopped: a supplied artwork title already exists for its master';
  END IF;
  IF EXISTS (
    SELECT 1 FROM v4_photo_seed p
    LEFT JOIN v4_artwork_seed a USING(master_name,title_es)
    WHERE a.title_es IS NULL
  ) THEN
    RAISE EXCEPTION 'V4 stopped: a supplied photo does not match one of the 21 supplied works';
  END IF;
  IF EXISTS (
    SELECT 1 FROM v4_artwork_seed a
    WHERE NOT EXISTS (SELECT 1 FROM v4_photo_seed p WHERE p.master_name=a.master_name AND p.title_es=a.title_es)
  ) THEN
    RAISE EXCEPTION 'V4 stopped: every supplied work must have at least one supplied photo';
  END IF;
  IF EXISTS (
    SELECT 1 FROM v4_artwork_seed a
    WHERE (SELECT count(*) FROM v4_photo_seed p WHERE p.master_name=a.master_name AND p.title_es=a.title_es AND p.is_primary) <> 1
  ) THEN
    RAISE EXCEPTION 'V4 stopped: each supplied work must have exactly one primary image';
  END IF;
END $$;

-- Fail before choosing stores if legacy cardinalities are ambiguous. The four
-- new masters may have no store yet (or one already created); more than one is
-- always an error. This avoids arbitrary store selection and duplicates.
DO $$
BEGIN
  IF EXISTS (
    SELECT n.name FROM (VALUES ('María Cabrera'),('Richard Valderrama'),('Gilberto Granja'),
                             ('Guillermo Lasso'),('José Castrillón'),('Jesús Caguasongo')) AS n(name)
    LEFT JOIN museo.masters m ON m.name=n.name AND m.is_active
    LEFT JOIN museo.stores s ON s.master_id=m.master_id
    GROUP BY n.name
    HAVING count(DISTINCT m.master_id) <> 1
       OR (n.name IN ('María Cabrera','Richard Valderrama') AND count(s.store_id) <> 1)
       OR (n.name NOT IN ('María Cabrera','Richard Valderrama') AND count(s.store_id) > 1)
  ) THEN
    RAISE EXCEPTION 'V4 stopped: each expected active master must exist; María Cabrera and Richard Valderrama must each have exactly one store, and each new master may have at most one';
  END IF;
END $$;

-- Reuse v1 stores for María and Richard; create only missing stores for the
-- four new masters. The CSV gives a proper workshop/store name but no English
-- translation, so both required name fields retain that exact supplied name.
INSERT INTO museo.stores(master_id,template_id,name_es,name_en,status)
SELECT m.master_id,t.template_id,x.workshop,x.workshop,'borrador'
FROM (SELECT DISTINCT master_name,workshop FROM v4_artwork_seed) x
JOIN museo.masters m ON m.name=x.master_name AND m.is_active
JOIN museo.store_templates t ON t.code='taller-clasico'
WHERE m.name IN ('Gilberto Granja','Guillermo Lasso','José Castrillón','Jesús Caguasongo')
  AND NOT EXISTS (SELECT 1 FROM museo.stores s WHERE s.master_id=m.master_id);

DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM (VALUES ('María Cabrera'),('Richard Valderrama'),('Gilberto Granja'),
                         ('Guillermo Lasso'),('José Castrillón'),('Jesús Caguasongo')) AS n(name)
    JOIN museo.masters m USING(name)
    WHERE m.is_active AND (SELECT count(*) FROM museo.stores s WHERE s.master_id=m.master_id) <> 1
  ) THEN RAISE EXCEPTION 'V4 stopped: every one of the six active CSV masters must have exactly one associated store; no arbitrary store was selected'; END IF;
END $$;

CREATE TEMP TABLE v4_master_store_seed (
  master_name text PRIMARY KEY,
  store_id bigint NOT NULL UNIQUE
) ON COMMIT DROP;

INSERT INTO v4_master_store_seed(master_name,store_id)
SELECT m.name,s.store_id
FROM v4_artwork_seed x
JOIN museo.masters m ON m.name=x.master_name AND m.is_active
JOIN museo.stores s USING(master_id)
GROUP BY m.name,s.store_id;

DO $$
BEGIN
  IF (SELECT count(*) FROM v4_master_store_seed) <> 6 THEN
    RAISE EXCEPTION 'V4 stopped: expected one resolved existing store per active CSV master';
  END IF;
END $$;

INSERT INTO museo.artworks
  (store_id,title_es,title_en,history_es,history_en,shape_id,style_id,
   width_cm,height_cm,depth_cm,sale_mode,price,currency,stock,status)
SELECT s.store_id,x.title_es,x.title_es,x.description_es,x.description_en,sh.shape_id,st.style_id,
       x.width_cm,x.height_cm,x.depth_cm,'subasta',NULL,NULL,x.stock,'borrador'
FROM v4_artwork_seed x
JOIN v4_master_store_seed ms ON ms.master_name=x.master_name
JOIN museo.stores s ON s.store_id=ms.store_id
JOIN museo.shapes sh ON sh.code=x.shape_code
JOIN museo.styles st ON st.code=x.style_code
WHERE NOT EXISTS (SELECT 1 FROM museo.artworks a WHERE a.store_id=s.store_id AND a.title_es=x.title_es);

INSERT INTO museo.artwork_colors(artwork_id,color_id)
SELECT a.artwork_id,c.color_id
FROM v4_artwork_seed x
JOIN v4_master_store_seed ms ON ms.master_name=x.master_name
JOIN museo.stores s ON s.store_id=ms.store_id
JOIN museo.artworks a ON a.store_id=s.store_id AND a.title_es=x.title_es
CROSS JOIN LATERAL unnest(x.color_codes) AS color(code)
JOIN museo.colors c USING(code)
ON CONFLICT DO NOTHING;

INSERT INTO museo.artwork_motifs(artwork_id,motif_id)
SELECT a.artwork_id,mt.motif_id
FROM v4_artwork_seed x
JOIN v4_master_store_seed ms ON ms.master_name=x.master_name
JOIN museo.stores s ON s.store_id=ms.store_id
JOIN museo.artworks a ON a.store_id=s.store_id AND a.title_es=x.title_es
CROSS JOIN LATERAL unnest(x.motif_codes) AS motif(code)
JOIN museo.motifs mt USING(code)
ON CONFLICT DO NOTHING;

INSERT INTO museo.photos(artwork_id,file_url,sort_order,is_primary)
SELECT a.artwork_id,p.file_url,p.sort_order,p.is_primary
FROM v4_photo_seed p
JOIN v4_artwork_seed x USING(master_name,title_es)
JOIN v4_master_store_seed ms ON ms.master_name=x.master_name
JOIN museo.stores s ON s.store_id=ms.store_id
JOIN museo.artworks a ON a.store_id=s.store_id AND a.title_es=x.title_es
WHERE NOT EXISTS (SELECT 1 FROM museo.photos ph WHERE ph.artwork_id=a.artwork_id AND ph.file_url=p.file_url);
