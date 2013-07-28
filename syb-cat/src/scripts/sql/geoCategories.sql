DROP TABLE IF EXISTS graph.geocategories;
CREATE TABLE graph.geocategories
(
  category character varying(256)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE graph.geocategories
  OWNER TO kiko;


INSERT INTO graph.geoCategories(category)
SELECT category FROM graph.allCategories WHERE lower(category) LIKE 'cities%'
AND lower(category) NOT LIKE '%district%'
UNION
SELECT category FROM graph.allCategories WHERE lower(category) LIKE 'metropolitan areas of%'
UNION
SELECT category FROM graph.allCategories WHERE lower(category) LIKE 'geography of%'
UNION
SELECT category FROM graph.allCategories WHERE lower(category) LIKE 'counties of%'
UNION
SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%oblasts of%'
UNION
SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%voivodeships of%'
UNION
SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%regions of%'
UNION
SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%towns %'
AND lower(category) NOT LIKE '%chinatown%'
UNION
SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%provinces of%'
AND lower(category) NOT LIKE '%ecclesiastical%'
UNION
SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%prefectures of%'
UNION
SELECT category FROM graph.allCategories WHERE lower(category) LIKE 'provinces of%'
UNION
SELECT category FROM graph.allCategories WHERE lower(category) LIKE 'states of%';