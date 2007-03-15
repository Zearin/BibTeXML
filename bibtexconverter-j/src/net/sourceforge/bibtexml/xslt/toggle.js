function toggleAbstract(citekey){
  abs = eval(document.getElementById("abstract_"+citekey));
  if(abs.style.display=="none"){
      abs.style.display="block";
  } else {
    abs.style.display="none";
  }
}