/*<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta http-equiv='Pragma' content='no-cache'/>
<meta http-equiv='Cache-Control' content='no-cache'/>	
<meta http-equiv="Expires" content="-1"/>*/
function checkValidation()
{
	var frm=document.Login;
	if (frm.userId.value=="")
	{
		alert("Please enter User ID.");
		frm.userId.focus();
		return false;
	}
	else if(!isAlphaNumeric(frm.userId.value))
	{
		alert("Please enter only alphanumeric value in User ID.");
		frm.userId.focus();
		return false;
	}
	
	if (frm.Login_user_password.value=="")
	{
		alert("Please enter Password.");
		frm.Login_user_password.focus();
		return false;
	}
	else if(!isAllowedInPassword(frm.Login_user_password.value))
	{
		alert("Invalid Password.");
		frm.Login_user_password.value="";
                frm.Login_user_password.focus();
		return false;
	}
	return true;
}

function isAlphaNumeric(thisValue) 
{
	var strValidChars = ".-_0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	var strChar;
	var blnResult = true;
	var t1 =thisValue;	
	for (i = 0; i < t1.length && blnResult == true; i++)
	{
		strChar = t1.charAt(i);
		if (strValidChars.indexOf(strChar) == -1)
		{
			blnResult = false;
			return false;			
		}
	}
	return true;
}

function isAllowedInPassword(thisValue)
{
	var strValidChars = ".-_0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!@#$%^&*-_=+";
	var strChar;
	var blnResult = true;
	var t1 =thisValue;	
	for (i = 0; i < t1.length && blnResult == true; i++)
	{
		strChar = t1.charAt(i);
		if (strValidChars.indexOf(strChar) == -1)
		{
			blnResult = false;
			return false;			
		}
	}
	return true;
}
function frmValidation()
{
	var frm=document.forgotPasswordCreate;
	if (frm.userId.value=="")
	{
		alert("Please enter User ID.");
		frm.userId.focus();
		return false;
	}
}	
/*<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta http-equiv='Pragma' content='no-cache'/>
<meta http-equiv='Cache-Control' content='no-cache'/>	
<meta http-equiv="Expires" content="-1"/>*/