
SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%musician%' 
AND lower(category) NOT LIKE '%albums%' 
AND lower(category) NOT LIKE '%songs%' 
AND lower(category) NOT LIKE '%(musician)%'
AND lower(category) NOT LIKE '%film%' 
AND lower(category) NOT LIKE '%biograph%' 
AND lower(category) NOT LIKE '%book%' 
AND lower(category) NOT LIKE '%expatriate%' 
AND lower(category) NOT LIKE '%hypocritical%' 
AND lower(category) NOT LIKE '%lists%' 
AND lower(category) NOT LIKE '%video game%' 

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%musical groups%' 
AND lower(category) NOT LIKE '%albums%' 
AND lower(category) NOT LIKE '%songs%' 
AND lower(category) NOT LIKE '%(musician)%'
AND lower(category) NOT LIKE '%film%' 
AND lower(category) NOT LIKE '%biograph%' 
AND lower(category) NOT LIKE '%book%' 
AND lower(category) NOT LIKE '%expatriate%' 
AND lower(category) NOT LIKE '%hypocritical%' 
AND lower(category) NOT LIKE '%lists%' 
AND lower(category) NOT LIKE '%comics%' 



ORDER BY category;

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%singer%' 
AND lower(category) NOT LIKE '%album%' 

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%blues%' 
AND lower(category) NOT LIKE '%album%' 

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%rhythm & blues%' 
AND lower(category) NOT LIKE '%album%' 

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%jazz musician%' 

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%classical musician%' 

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%rock musician%' 

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%pop musician%'
 
SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%folk musician%' 

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%rapper%' 
AND lower(category) NOT LIKE '%album%' 

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%techno %' 
AND lower(category) NOT LIKE '%album%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%electronic musician%' 
AND lower(category) NOT LIKE '%album%'  

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%country musician%' 
AND lower(category) NOT LIKE '%album%' 
AND lower(category) NOT LIKE '%song%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%funk musician%' 
AND lower(category) NOT LIKE '%album%' 

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%opera singer%' 
AND lower(category) NOT LIKE '%album%'


SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%conductor%' 
AND lower(category) NOT LIKE '%semiconductor%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%dancer%' 
AND lower(category) NOT LIKE '%erotic%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%swimmer%' 

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%skier%' 

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%cyclist%' 
AND lower(category) NOT LIKE '%motor%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%triathlete%' 
AND lower(category) NOT LIKE '%university%'
AND lower(category) NOT LIKE '%athlete of the year%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%biathlete%' 

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%track and field%' 
AND lower(category)  LIKE '%athlete%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%water polo player%' 

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%gymnasts%' 

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%wrestler%' 

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%tennis player%' 

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%football player%' 

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%soccer player%' 

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%cricket player%' 

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%baseball player%' 

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%hockey player%' 

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%water polo player%' 


SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%racer%' 
AND lower(category) NOT LIKE '%album%'
AND lower(category) NOT LIKE '%raytracer%'







SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%sports%' 
AND lower(category) NOT LIKE '%lists%'
AND lower(category) NOT LIKE '%expatriate%'
AND lower(category) NOT LIKE '%expatriate%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%motorcycle rac%' 



AND lower(category) NOT LIKE '%lists%'
AND lower(category) NOT LIKE '%lists%'







SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%songwriter%' 
AND lower(category) NOT LIKE '%album%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%composer%' 
AND lower(category) NOT LIKE '%album%'
AND lower(category) NOT LIKE 'list%'
AND lower(category) NOT LIKE 'works by%'




SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%intelligence%' 
AND lower(category) NOT LIKE '%albums%' 
AND lower(category) NOT LIKE '%artificial intelligence%' 
AND lower(category) NOT LIKE '%ambient intelligence%'
AND lower(category) NOT LIKE '%automotive intelligence%' 
AND lower(category) NOT LIKE '%business intelligence%' 
AND lower(category) NOT LIKE '%animal%' 
AND lower(category) NOT LIKE '%competitive%' 
AND lower(category) NOT LIKE 'intelligence' 
AND lower(category) NOT LIKE 'price' 


SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%sportspeople%' 
AND lower(category) NOT LIKE '%expatriate%' 

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%player%' 

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%writer%' 
AND lower(category) NOT LIKE '%song%'
AND lower(category) NOT LIKE '%video%'
AND lower(category) NOT LIKE '%chill%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%leader%' AND lower(category) LIKE '%religious%' 

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%politician%' 

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%celebrit%' 

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%personalit%' 

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%model%' 
AND (lower(category) LIKE '%female%' OR lower(category) LIKE '%male%')

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%ecuadorian%' 
AND (lower(category) LIKE '%female%' OR lower(category) LIKE '%male%')


SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%wine%' 

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%cocktail%' 

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%food%' 

 SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%economist%' 

 SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%philosopher%' 

 SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%scientist%' 

 SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%actor%' 
 AND lower(category) NOT LIKE '%factor%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%actress%' 
AND lower(category) NOT LIKE '%album%' 

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%leader%' 
AND lower(category) LIKE '%religious%' 

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%leader%' 
AND lower(category) LIKE '%political parties%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '% artist%'
AND lower(category) NOT LIKE '%record%' 
AND lower(category) NOT LIKE '%album%' 
AND lower(category) NOT LIKE '%martial%' 
AND lower(category) NOT LIKE '%make-up%' 
AND lower(category) NOT LIKE '%music%' 
AND lower(category) NOT LIKE '%film%'
AND lower(category) NOT LIKE '%discograph%' 
AND lower(category) NOT LIKE '%gymnast%' 

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '% painter%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '% poet%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '% sculptor%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '% terrorist%'
AND lower(category) NOT LIKE '%comic%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%political parties%'
AND lower(category) NOT LIKE '%comic%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%people%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%people from%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%military leader%'
AND lower(category) NOT LIKE '%film%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%military officer%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%military unit%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%political organizations%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE 'politics of%'
AND lower(category) NOT LIKE '%books%'
AND lower(category) NOT LIKE '%lists%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%politics%'
AND lower(category) NOT LIKE '%books%'
AND lower(category) NOT LIKE '%lists%'
AND lower(category) NOT LIKE '%politics of%'


SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%religious organizations%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%organizations%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE 'cities%'
AND lower(category) NOT LIKE '%district%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE 'metropolitan areas of%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE 'geography of%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE 'counties of%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%oblasts of%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%voivodeships of%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%regions of%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%towns %'
AND lower(category) NOT LIKE '%chinatown%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%provinces of%'
AND lower(category) NOT LIKE '%ecclesiastical%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%prefectures of%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE 'provinces of%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE 'states of%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%companies of%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%chief executives%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%presidents of%'
AND lower(category) NOT LIKE '%college%'
AND lower(category) NOT LIKE '%university%'
AND lower(category) NOT LIKE '%compan%'
AND lower(category) NOT LIKE '%book%'
AND lower(category) NOT LIKE '%program%'
AND lower(category) NOT LIKE '%news%'
AND lower(category) NOT LIKE '%site%'
AND lower(category) NOT LIKE '%fictional%'
AND lower(category) NOT LIKE '%cbs%'
AND lower(category) NOT LIKE '%organization%'
AND lower(category) NOT LIKE '%church%'
AND lower(category) NOT LIKE '%institute%'
AND lower(category) NOT LIKE '%ccc%'
AND lower(category) NOT LIKE '%society%'
AND lower(category) NOT LIKE '%association%'
AND lower(category) NOT LIKE '%club%'
AND lower(category) NOT LIKE '%federation%'
AND lower(category) NOT LIKE '%council%'
AND lower(category) NOT LIKE '%party%'
AND lower(category) NOT LIKE '%academy%'
AND lower(category) NOT LIKE '%parish%'
AND lower(category) NOT LIKE '%institution%'
AND lower(category) NOT LIKE '%committee%'
AND lower(category) NOT LIKE '%trust%'
AND lower(category) NOT LIKE '%brotherhood%'
AND lower(category) NOT LIKE '%union%'


SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%minister%'
AND lower(category) NOT LIKE '%administer%'
AND lower(category) NOT LIKE '%episode%'
AND lower(category) NOT LIKE '%character%'
AND lower(category) NOT LIKE '%presbyterian%'
AND lower(category) NOT LIKE '%mennonite%'
AND lower(category) NOT LIKE '%baptist%'
AND lower(category) NOT LIKE '%christian%'
AND lower(category) NOT LIKE '%nazarene%'



SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%central bank%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%policy organizations%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%journalist%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%journalism organization%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%trade group%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%trade unions in%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%cocktails%'
AND lower(category) NOT LIKE '%lists%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%distilled beverages%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%beverages%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%dishes%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%food and drink%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%chefs%'


policy organizations
minister
central bank
official
journalist
trade group
union


SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%technology%'
AND lower(category) NOT LIKE '%alumni%'

SELECT category FROM graph.allCategories WHERE lower(category) LIKE '%terroris%' 
AND lower(category) NOT LIKE '%book%'














