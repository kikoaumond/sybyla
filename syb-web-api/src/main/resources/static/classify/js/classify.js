var BASE_URL='http://demo.sybyla.com/category/';

function classify(text) {
	$('#result').html('');
	var dest = BASE_URL;
	var data={};
	data.text=text
	var postRequest = $.post(dest, 
	                         data, 
	                         function(responseData) {
	                             console.log(responseData);
	                             $('#result').html(responseData.apiResponse.categories);
	                         });
}

function displayResponse(responseData){
	$('#result').html('');
	if (responseData.apiResponse.categories){
		var categoryArray  = responseData.apiResponse.categories;
		var output='';
		for ( var i in categoryArray) {
			var category = categoryArray[i];
			output += 'Category: <b>'+category.category+'</b> ';
			
			if (category.categoryIAB && category.categoryIAB != ''){
				output += 'IAB Category: <b>'+category.categoryIAB+'</b> ';
			}
			
			if (category.categoryDetail && category.categoryDetail != ''){		
				output += 'Detail Category: <b>'+category.categoryIAB+'</b> ';
			}
			
			if (category.categoryIABDetail && category.categoryIABDetail != ''){
				output += 'IAB Detail Category: <b>'+category.categoryIABDetail+'</b> ';
			}
			
			if (category.geo && category.geo != ''){
				output += 'Location: <b>'+category.geo+'</b> ';
			}
			
			if (category.geoDetail && category.geoDetail != ''){
				output += 'Detail Location: <b>'+category.geoDetail+'</b> ';
			}
			
			if (category.chrono && category.chrono != ''){
				output += 'Chronological Information: <b>'+category.chrono+'</b> ';
			}
			
			if (category.chronoDetail && category.chronoDetail != ''){
				output += 'Detail Chronological Information: <b>'+category.geo+'</b> ';
			}
			output +='<br>';

		}
		$('#result').html(output);
	}
}



