//##############################
// jQuery Custom Radio-buttons and Checkbox; basically it's styling/theming for Checkbox and Radiobutton elements in forms
// Date of Release: 13th March 10
// Version: 0.8
/*
 USAGE:
	$(document).ready(function(){
		$(":radio").behaveLikeCheckbox();
	}
*/

var elmHeight = "25";	// should be specified based on image size

// Extend JQuery Functionality For Custom Radio Button Functionality
$.extend($.fn, {
dgStyle: function()
{
	// Initialize with initial load time control state
	$.each($(this), function(){
		var elm	=	$(this).find('input').first();//children().get(0);
		elmType = $(elm).attr("type");
		$(this).data('type',elmType);
		$(this).data('checked',$(elm).attr("checked") ? true : false);
		if($(elm).attr("checked") == "checked")
			$(this).addClass("checked");
		$(this).dgClear();
	});
	$(this).mousedown(function() { $(this).dgEffect(); });
	$(this).mouseup(function() { $(this).dgHandle(); });	
},
dgClear: function()
{
	if($(this).data("checked") == true)
	{
		$(this).addClass("checked");
		}
	else
	{
		$(this).removeClass("checked");
		}	
},
dgEffect: function()
{
	if($(this).data("checked") == true)
		$(this).removeClass('checked');
	else
		$(this).removeClass('checked');
},
dgHandle: function()
{
	var elm	=	$(this).find('input').first();//children().get(0);
	if($(this).data("checked") == true)
		$(elm).dgUncheck(this);
	else
		$(elm).dgCheck(this);
	
	if($(this).data('type') == 'radio')
	{
		$.each($("input[name='"+$(elm).attr("name")+"']"),function()
		{
			if(elm.attr('id') != this.id)
				$(this).dgUncheck(-1);
			else
				$(this).dgCheck($(this).parents('.radio').first());
		});
	}
},
dgCheck: function(div)
{
	$(this).attr("checked",true);
	$(div).data('checked',true).addClass("checked");
},
dgUncheck: function(div)
{
	$(this).attr("checked",false);
	if(div != -1)
		$(div).data('checked',false).removeClass("checked");
	else
		$(this).parents('.radio').data("checked",false).removeClass("checked");
}
});