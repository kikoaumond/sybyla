
var labelType, useGradients, nativeTextSupport, animate;
var BASE_URL='http://localhost/graph/';//'ec2-23-23-32-211.compute-1.amazonaws.com/graph/';
var WHY_URL='http://localhost/why/';

var tree;

(function() {
  var ua = navigator.userAgent,
      iStuff = ua.match(/iPhone/i) || ua.match(/iPad/i),
      typeOfCanvas = typeof HTMLCanvasElement,
      nativeCanvasSupport = (typeOfCanvas == 'object' || typeOfCanvas == 'function'),
      textSupport = nativeCanvasSupport 
        && (typeof document.createElement('canvas').getContext('2d').fillText == 'function');
  //I'm setting this based on the fact that ExCanvas provides text support for IE
  //and that as of today iPhone/iPad current text support is lame
  labelType = (!nativeCanvasSupport || (textSupport && !iStuff))? 'Native' : 'HTML';
  nativeTextSupport = labelType == 'Native';
  useGradients = nativeCanvasSupport;
  animate = !(iStuff || !nativeCanvasSupport);
})();

var Log = {
  elem: false,
  write: function(text){
    if (!this.elem) 
      this.elem = document.getElementById('log');
    if (this.elem){
    	this.elem.innerHTML = text;
    	this.elem.style.left = (500 - this.elem.offsetWidth / 2) + 'px';
    }
  }
};

function getParameterByName(name)
{
  name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
  var regexS = "[\\?&]" + name + "=([^&#]*)";
  var regex = new RegExp(regexS);
  var results = regex.exec(window.location.href);
  if(results == null)
    return "";
  else
    return decodeURIComponent(results[1].replace(/\+/g, " "));
}


function getResponse()
{
	var postURL = BASE_URL;
	var urlParam = getParameterByName('html.url');
	var termParam = getParameterByName('term');
	var dest;
	if (termParam) {
		dest = postURL+'?term='+termParam;
	} else {
		dest=postURL+'?html.url='+urlParam;
	}
	//$.post(url,function success(response,status,xhr){s(response,status,xhr);} ,'text');
	/*
	$.ajax({
    url: dest,
    type: 'GET',
    crossDomain: true,
    dataType: 'jsonp',
    success: function(response) { s(response); },
    error: function(response) { s(response); }
	});*/
	$.get(dest,
   function(data){
     init(data);
   }, "jsonp");
}


function getRelatedTerms(term,nTerms) {
	var dest = BASE_URL+'?term='+term+'&nTerms='+nTerms;
	$.get(dest,
   		  function(data){
     		init(data);
   		  }, 
   		  "jsonp");
}

function why(data){
	if (data.apiResponse){
		$("#why").html(data.apiResponse.why.link);
	}
}

function init(data){
	    //init data
    var json = data.apiResponse.tree; 
    tree = data.apiResponse.tree; 
    $('#infovis-canvaswidget').remove();   
    //init RGraph
   
    var rgraph = new $jit.RGraph({
        //Where to append the visualization
        injectInto: 'infovis',
        //Optional: create a background canvas that plots
        //concentric circles.
        background: {
          CanvasStyles: {
            strokeStyle: '#555'
          }
        },
        //Add navigation capabilities:
        //zooming by scrolling and panning.
        Navigation: {
          enable: true,
          panning: true,
          zooming: 10
        },
        //Set Node and Edge styles.
        Node: {
            color: '#ddeeff'
        },
        
        Edge: {
          color: '#C17878',
          lineWidth:0.5
        },

        onBeforeCompute: function(node){
            Log.write("centering " + node.name + "...");
            //Add the relation list in the right column.
            //This list is taken from the data property of each JSON node.
            //$jit.id('inner-details').innerHTML = node.data.relation;
        },
        
        //Add the name of the node in the correponding label
        //and a click handler to move the graph.
        //This method is called once, on label creation.
        onCreateLabel: function(domElement, node){
            domElement.innerHTML = node.name;
            domElement.onclick = function(){
                rgraph.onClick(node.id, {
                    onComplete: function() {
                        Log.write("done");
                    }
                });
                var dest = WHY_URL+"?term1="+tree.name+"&term2="+node.name;
				$.get(dest,function(data){why(data);},"jsonp");
            };/*
            domElement.ondblclick =  function(){
            	url = 'https://www.google.com/search?q='+node.name;
				newwindow=window.open(url,node.name,'height=1000,width=800');
				if (window.focus) {
					newwindow.focus();
				}
				return false;
            };*/
            domElement.ondblclick = function() {
            	//var dest = 'http://localhost:8984/graph/?nTerms=10&term='+node.name;
				var nTerms = $("#nTerms").val();
				if (nTerms==null || nTerms=="") {
    		    	nTerms = 8;
    		    }
				var dest = BASE_URL+"?term="+node.name+"&nTerms="+nTerms;
				$("#term").val(node.name);
				$("#why").html("");
				$.get(dest,function(data){init(data);},"jsonp");
            }
        },
        //Change some label dom properties.
        //This method is called each time a label is plotted.
        onPlaceLabel: function(domElement, node){
            var style = domElement.style;
            style.display = '';
            style.cursor = 'pointer';

            if (node._depth == 0) {
                style.fontSize = "1.0em";
                style.color = "#FFFFFF";
            
            } else if(node._depth == 1){
                style.fontSize = "1.0em";
                style.color = "#DDAA00";
            
            }else if(node._depth > 1){
                style.fontSize = "1.0em";
                style.color = "#AAAACC";
            
            } else {
                //style.display = 'none';
                 style.fontSize = "0.8em";
                style.color = "#AAAAAA";
            }

            var left = parseInt(style.left);
            var w = domElement.offsetWidth;
            style.left = (left - w / 2) + 'px';
        }
    });
    
    //clear graph if already populated
    rgraph.graph.nodeList.removeData();
    rgraph.graph.empty();
    rgraph.canvas.clear();

    //load JSON data
    rgraph.loadJSON(json);
    //trigger small animation
    rgraph.graph.eachNode(function(n) {
      var pos = n.getPos();
      pos.setc(-200, -200);
    });
    rgraph.compute('end');
    rgraph.fx.animate({
      modes:['polar'],
      duration: 2000
    });
    
    //end
    //append information about the root relations in the right column
    //$jit.id('inner-details').innerHTML = rgraph.graph.getNode(rgraph.root).data.relation;
}
