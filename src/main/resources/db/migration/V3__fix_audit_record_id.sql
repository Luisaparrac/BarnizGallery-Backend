-- Fix audit_log.record_id to use the primary key column for each audited table.
-- Existing audit_log rows are intentionally left unchanged.
CREATE OR REPLACE FUNCTION museo.audit_row_change()
RETURNS trigger
LANGUAGE plpgsql
SET search_path = museo, pg_catalog
AS $$
DECLARE
  v_actor bigint;
  v_old jsonb;
  v_new jsonb;
  v_id text;
BEGIN
  v_actor := nullif(current_setting('app.user_id', true), '')::bigint;

  IF TG_OP <> 'INSERT' THEN
    v_old := to_jsonb(OLD) - 'password_hash';
  END IF;
  IF TG_OP <> 'DELETE' THEN
    v_new := to_jsonb(NEW) - 'password_hash';
  END IF;

  v_id := COALESCE(v_new->>TG_ARGV[0], v_old->>TG_ARGV[0]);

  INSERT INTO museo.audit_log(table_name, record_id, action, changed_by, old_data, new_data)
  VALUES (TG_TABLE_NAME, v_id, TG_OP, v_actor, v_old, v_new);

  RETURN NULL;
END;
$$;

DROP TRIGGER IF EXISTS users_audit ON museo.users;
CREATE TRIGGER users_audit AFTER INSERT OR UPDATE OR DELETE ON museo.users
  FOR EACH ROW EXECUTE FUNCTION museo.audit_row_change('user_id');

DROP TRIGGER IF EXISTS masters_audit ON museo.masters;
CREATE TRIGGER masters_audit AFTER INSERT OR UPDATE OR DELETE ON museo.masters
  FOR EACH ROW EXECUTE FUNCTION museo.audit_row_change('master_id');

DROP TRIGGER IF EXISTS stores_audit ON museo.stores;
CREATE TRIGGER stores_audit AFTER INSERT OR UPDATE OR DELETE ON museo.stores
  FOR EACH ROW EXECUTE FUNCTION museo.audit_row_change('store_id');

DROP TRIGGER IF EXISTS artworks_audit ON museo.artworks;
CREATE TRIGGER artworks_audit AFTER INSERT OR UPDATE OR DELETE ON museo.artworks
  FOR EACH ROW EXECUTE FUNCTION museo.audit_row_change('artwork_id');

DROP TRIGGER IF EXISTS auctions_audit ON museo.auctions;
CREATE TRIGGER auctions_audit AFTER INSERT OR UPDATE OR DELETE ON museo.auctions
  FOR EACH ROW EXECUTE FUNCTION museo.audit_row_change('auction_id');

DROP TRIGGER IF EXISTS orders_audit ON museo.orders;
CREATE TRIGGER orders_audit AFTER INSERT OR UPDATE OR DELETE ON museo.orders
  FOR EACH ROW EXECUTE FUNCTION museo.audit_row_change('order_id');

DROP TRIGGER IF EXISTS payments_audit ON museo.payments;
CREATE TRIGGER payments_audit AFTER INSERT OR UPDATE OR DELETE ON museo.payments
  FOR EACH ROW EXECUTE FUNCTION museo.audit_row_change('payment_id');

DROP TRIGGER IF EXISTS custom_offers_audit ON museo.custom_offers;
CREATE TRIGGER custom_offers_audit AFTER INSERT OR UPDATE OR DELETE ON museo.custom_offers
  FOR EACH ROW EXECUTE FUNCTION museo.audit_row_change('custom_offer_id');

DROP TRIGGER IF EXISTS app_settings_audit ON museo.app_settings;
CREATE TRIGGER app_settings_audit AFTER INSERT OR UPDATE OR DELETE ON museo.app_settings
  FOR EACH ROW EXECUTE FUNCTION museo.audit_row_change('setting_key');

DROP TRIGGER IF EXISTS master_verifications_audit ON museo.master_verifications;
CREATE TRIGGER master_verifications_audit AFTER INSERT OR UPDATE OR DELETE ON museo.master_verifications
  FOR EACH ROW EXECUTE FUNCTION museo.audit_row_change('verification_id');
