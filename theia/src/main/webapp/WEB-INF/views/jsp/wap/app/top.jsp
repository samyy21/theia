<script type="text/javascript">
	function getWidth()
	{
	  xWidth = null;
	  if(window.screen != null)
	    xWidth = window.screen.availWidth;
	
	  if(window.innerWidth != null)
	    xWidth = window.innerWidth;
	
	  if(document.body != null)
	    xWidth = document.body.clientWidth;
	
	  return xWidth;
	}
	document.cookie="screen.width=" + getWidth();
</script>