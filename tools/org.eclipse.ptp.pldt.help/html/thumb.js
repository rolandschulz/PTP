
// Javascript to provide a convenient way to show a thumbnail of an image, with a link to the full-size image
// Default height is 120 if the second arg is omitted
//Note that IE uses the alt tag for hover but Firefox more correctly uses the title tag for hover text.
//Usage: <script> thumb("images/foo.gif")</script>
function thumb(url,height){
   if (!height) {height=120;}
   document.write ("<a href='"+url+"'><img src='"+url+"' height="+height+" title='Click for a full-size image' alt='Click for a full-size image'></a>");
}
// simple imbedding of image, in default full size, with break preceeding
// (easy to replace the above with this one)
//Usage: <script> full("images/foo.gif")</script>
function full(url){
   document.write ("<img src='"+url+"'>");
}

