CREATE INDEX comments_idx ON menu_review USING GIN(to_tsvector('english', comments));
