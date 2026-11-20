-- MUSEO v5: point the 32 photos loaded by V4 to the WebP files the frontend serves today
-- (public/obras/<master>/<artwork>-<n>.webp). Only file_url changes; ids, order and primary flags stay.

UPDATE museo.photos SET file_url = '/obras/granja/bargueno-colonial-1.webp' WHERE file_url = '/obras/Gilberto Granja/GilbertoGranja - Bargueño Colonial (1).webp';
UPDATE museo.photos SET file_url = '/obras/granja/bargueno-colonial-2.webp' WHERE file_url = '/obras/Gilberto Granja/GilbertoGranja - Bargueño Colonial (2).webp';
UPDATE museo.photos SET file_url = '/obras/granja/bargueno-1.webp' WHERE file_url = '/obras/Gilberto Granja/GilbertoGranja - Bargueño.avif';
UPDATE museo.photos SET file_url = '/obras/granja/jarron-flora-azul-1.webp' WHERE file_url = '/obras/Gilberto Granja/GilbertoGranja - Jarron Flora Azul (1).webp';
UPDATE museo.photos SET file_url = '/obras/granja/jarron-flora-azul-2.webp' WHERE file_url = '/obras/Gilberto Granja/GilbertoGranja - Jarron Flora Azul (2).webp';
UPDATE museo.photos SET file_url = '/obras/granja/jarron-flora-dorada-fondo-blanco-1.webp' WHERE file_url = '/obras/Gilberto Granja/GilbertoGranja - Jarron Flora Dorada Fondo Blanco.webp';
UPDATE museo.photos SET file_url = '/obras/granja/jarron-flora-dorada-fondo-negro-1.webp' WHERE file_url = '/obras/Gilberto Granja/GilbertoGranja - Jarron Flora Dorada Fondo Negro.webp';
UPDATE museo.photos SET file_url = '/obras/lasso/cuadro-tradicion-1.webp' WHERE file_url = '/obras/Guillermo Lasso/GuillermoLasso - Cuadro Tradición.avif';
UPDATE museo.photos SET file_url = '/obras/lasso/mascara-con-disenos-precolombinos-1.webp' WHERE file_url = '/obras/Guillermo Lasso/GuillermoLasso - Máscara con Diseños Precolombinos.avif';
UPDATE museo.photos SET file_url = '/obras/caguasongo/sol-de-los-pastos-1.webp' WHERE file_url = '/obras/JesusCaguasongo/JesusCaguasango - Sol de los Pastos.avif';
UPDATE museo.photos SET file_url = '/obras/caguasongo/toro-talla-1.webp' WHERE file_url = '/obras/JesusCaguasongo/JesusCaguasongo - Toro Talla - 40cmx20cmx14cm.avif';
UPDATE museo.photos SET file_url = '/obras/caguasongo/galeras-1.webp' WHERE file_url = '/obras/JesusCaguasongo/JesusCaguasongo -- Galeras.avif';
UPDATE museo.photos SET file_url = '/obras/castrillon/cultura-y-tradicion-1.webp' WHERE file_url = '/obras/Jose Castrillon/JoseCastrillon - Cultura y Tradicion.avif';
UPDATE museo.photos SET file_url = '/obras/castrillon/familia-papagayo-1.webp' WHERE file_url = '/obras/Jose Castrillon/JoseCastrillon - Familia Papagayo - 67cmx33cmx25cm.avif';
UPDATE museo.photos SET file_url = '/obras/castrillon/mascara-musico-1.webp' WHERE file_url = '/obras/Jose Castrillon/JoseCastrillon - Mascara musico.avif';
UPDATE museo.photos SET file_url = '/obras/cabrera/mensajero-ancestral-1.webp' WHERE file_url = '/obras/Maria Cabrera/Maria Cabrera - Mensajero Ancestral (1).png';
UPDATE museo.photos SET file_url = '/obras/cabrera/mensajero-ancestral-2.webp' WHERE file_url = '/obras/Maria Cabrera/Maria Cabrera - Mensajero Ancestral (2).png';
UPDATE museo.photos SET file_url = '/obras/cabrera/mensajero-ancestral-3.webp' WHERE file_url = '/obras/Maria Cabrera/Maria Cabrera - Mensajero Ancestral (3).png';
UPDATE museo.photos SET file_url = '/obras/cabrera/mensajero-ancestral-4.webp' WHERE file_url = '/obras/Maria Cabrera/Maria Cabrera - Mensajero Ancestral (4).png';
UPDATE museo.photos SET file_url = '/obras/cabrera/sabiduria-ancestral-1.webp' WHERE file_url = '/obras/Maria Cabrera/Maria Cabrera - Sabiduria Ancestral (1).png';
UPDATE museo.photos SET file_url = '/obras/cabrera/sabiduria-ancestral-2.webp' WHERE file_url = '/obras/Maria Cabrera/Maria Cabrera - Sabiduria Ancestral (2).png';
UPDATE museo.photos SET file_url = '/obras/cabrera/sabiduria-ancestral-3.webp' WHERE file_url = '/obras/Maria Cabrera/Maria Cabrera - Sabiduria Ancestral (3).png';
UPDATE museo.photos SET file_url = '/obras/cabrera/sabiduria-ancestral-4.webp' WHERE file_url = '/obras/Maria Cabrera/Maria Cabrera - Sabiduria Ancestral (4).png';
UPDATE museo.photos SET file_url = '/obras/cabrera/tradicion-1.webp' WHERE file_url = '/obras/Maria Cabrera/Maria Cabrera - Tradición (1).png';
UPDATE museo.photos SET file_url = '/obras/cabrera/tradicion-2.webp' WHERE file_url = '/obras/Maria Cabrera/Maria Cabrera - Tradición (2).png';
UPDATE museo.photos SET file_url = '/obras/cabrera/tradicion-3.webp' WHERE file_url = '/obras/Maria Cabrera/Maria Cabrera - Tradición (3).png';
UPDATE museo.photos SET file_url = '/obras/cabrera/tradicion-4.webp' WHERE file_url = '/obras/Maria Cabrera/Maria Cabrera - Tradición (4).png';
UPDATE museo.photos SET file_url = '/obras/valderrama/cuadro-mujer-y-tradicion-1.webp' WHERE file_url = '/obras/Richard Valderrama/RichardValderrama - Cuadro Mujer y Tradicion.jpeg';
UPDATE museo.photos SET file_url = '/obras/valderrama/jarron-clasico-1.webp' WHERE file_url = '/obras/Richard Valderrama/RichardValderrama - Jarron Clasico.jpeg';
UPDATE museo.photos SET file_url = '/obras/valderrama/jarron-colibri-1.webp' WHERE file_url = '/obras/Richard Valderrama/RichardValderrama - Jarron Colibri.jpeg';
UPDATE museo.photos SET file_url = '/obras/valderrama/paisaje-tipico-de-narino-1.webp' WHERE file_url = '/obras/Richard Valderrama/RichardValderrama - Paisaje tipico de Nariño - 34cmx46cmx3cm.avif';
UPDATE museo.photos SET file_url = '/obras/valderrama/plato-decorado-1.webp' WHERE file_url = '/obras/Richard Valderrama/RichardValderrama - Plato Decorado - 40cm de diametro.avif';

-- Fail (and roll the whole migration back) if a photo under /obras/ is still not a WebP file, or if any of the
-- 32 new paths is missing (some V4 paths already ended in .webp, so the first check alone would not see them).
DO $$
DECLARE
  v_not_webp integer;
  v_missing integer;
BEGIN
  SELECT count(*) INTO v_not_webp FROM museo.photos
  WHERE file_url LIKE '/obras/%' AND file_url NOT LIKE '%.webp';
  IF v_not_webp > 0 THEN
    RAISE EXCEPTION 'V5 stopped: % photo(s) under /obras/ are not WebP files', v_not_webp;
  END IF;

  SELECT count(*) INTO v_missing FROM (VALUES
      ('/obras/granja/bargueno-colonial-1.webp'),
      ('/obras/granja/bargueno-colonial-2.webp'),
      ('/obras/granja/bargueno-1.webp'),
      ('/obras/granja/jarron-flora-azul-1.webp'),
      ('/obras/granja/jarron-flora-azul-2.webp'),
      ('/obras/granja/jarron-flora-dorada-fondo-blanco-1.webp'),
      ('/obras/granja/jarron-flora-dorada-fondo-negro-1.webp'),
      ('/obras/lasso/cuadro-tradicion-1.webp'),
      ('/obras/lasso/mascara-con-disenos-precolombinos-1.webp'),
      ('/obras/caguasongo/sol-de-los-pastos-1.webp'),
      ('/obras/caguasongo/toro-talla-1.webp'),
      ('/obras/caguasongo/galeras-1.webp'),
      ('/obras/castrillon/cultura-y-tradicion-1.webp'),
      ('/obras/castrillon/familia-papagayo-1.webp'),
      ('/obras/castrillon/mascara-musico-1.webp'),
      ('/obras/cabrera/mensajero-ancestral-1.webp'),
      ('/obras/cabrera/mensajero-ancestral-2.webp'),
      ('/obras/cabrera/mensajero-ancestral-3.webp'),
      ('/obras/cabrera/mensajero-ancestral-4.webp'),
      ('/obras/cabrera/sabiduria-ancestral-1.webp'),
      ('/obras/cabrera/sabiduria-ancestral-2.webp'),
      ('/obras/cabrera/sabiduria-ancestral-3.webp'),
      ('/obras/cabrera/sabiduria-ancestral-4.webp'),
      ('/obras/cabrera/tradicion-1.webp'),
      ('/obras/cabrera/tradicion-2.webp'),
      ('/obras/cabrera/tradicion-3.webp'),
      ('/obras/cabrera/tradicion-4.webp'),
      ('/obras/valderrama/cuadro-mujer-y-tradicion-1.webp'),
      ('/obras/valderrama/jarron-clasico-1.webp'),
      ('/obras/valderrama/jarron-colibri-1.webp'),
      ('/obras/valderrama/paisaje-tipico-de-narino-1.webp'),
      ('/obras/valderrama/plato-decorado-1.webp')
  ) AS expected(file_url)
  WHERE NOT EXISTS (SELECT 1 FROM museo.photos p WHERE p.file_url = expected.file_url);
  IF v_missing > 0 THEN
    RAISE EXCEPTION 'V5 stopped: % of the 32 new photo paths were not applied', v_missing;
  END IF;
END $$;
