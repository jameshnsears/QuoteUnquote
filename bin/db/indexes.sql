BEGIN TRANSACTION;
DROP INDEX IF EXISTS `index_quotations_digest`;
CREATE UNIQUE INDEX index_quotations_digest
ON quotations(digest);
COMMIT;