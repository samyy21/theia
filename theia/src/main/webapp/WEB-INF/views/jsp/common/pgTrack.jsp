<script type="text/javascript" >
  try {
	  var url = '<%=request.getSession().getAttribute("pgTrackUrl") %>';
	  if(url!=null && url !='' && url!='null'){
		  var ele = document.createElement("img");
		  ele.src= url;  
	  }
  } catch(e){}
</script>