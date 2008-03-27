
function newWin(url) {
	window.open(url, 'install_ptp', 'menubar=no, toolbar=no, status=no, resizable=yes, location=no, scrollbars=yes');
}
// Javascript to provide a convenient way to show a thumbnail of an image, with a link to the full-size image
// Default height is 120 if the second arg is omitted
//Note that IE uses the alt tag for hover but Firefox more correctly uses the title tag for hover text.
function thumb(url,height){
   if (!height) {height=120;}
   document.write ("<br><a href='"+url+"'><img src='"+url+"' height="+height+" title='Click for a full-size image' alt='Click for a full-size image'></a>");
}
// simple imbedding of image, in default full size, with break preceeding
// (easy to replace the above with this one)
function full(url){
   document.write ("<br><img src='"+url+"'>");
}
//trying to abbreviate this: <a href="javascript:void(0)" onClick="newWin('images/foo.gif')">Click here</a>
// with usage like this:  <script> thumbNewWin("foo.gif") </script>
// this doesn't work.   Hmmmmm....
// I think it has to do with the quotes. i need 3 nesting levels so just " and ' doesn't cut it.
// I tried escaping but that doesn't seem to work either.  \"
function thumbNewWin(url,height){
   if (!height) {height=120;}
   msg="Click here to see a full-size image in a new window";
   //document.write ("<br><a href='javascript:void(0)' onClick='newWin(\""+url+"\")><img src='"+url+"' height="+height+" title='Click' alt='Click'></a>");
   anchor="<a href='javascript:void(0)' onClick='newWin(\""+url+"\")>";
   img="<img src='"+url+"' height="+height+" title='"+msg+"' alt='"+msg+"'>";
   //alert("anchor="anchor);
   //alert("img="+img);
   document.write ("<br>"+anchor+img+"</a>");
   
 }  

