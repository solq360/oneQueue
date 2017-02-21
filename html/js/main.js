(function(){

	$(document).ready(function () {
		//home scroll 
		var doc = $(document),
			win = $(window),
			
			h = $("#head"),
			b = $("#body"),
			f = $("#foot"),
			home = $("#home"),
			shoppingCart = $("#shoppingCart"),
			itemList = $("#itemList"),
			loadDom = $("#loadDom"),
			buyItemList = $("#buyItemList"),
			product = $("#product"),
			loading = false,
			touchStartY = 0,
			oldHeight = loadDom.height(),
			maxHeight = oldHeight*4,
			playAnim = false;
			
			//ad
			$('#ad').unslider({
				dots: true
			});
			//test 
			for(var i =0;i<6;i+=2){
				itemList.append(loadTpl("#tpl-item",{
					"title1":"title"+i,
					"title2":"title"+(i+1),
					"test":{
						"abc":"abc"
					}
				}));
			}

			for(var i =0;i<10;i++){
				buyItemList.append(loadTpl("#tpl-preItem",{
				 
				}));
			}
			
  		win.unbind("scroll").on('scroll',function(){
			 
			var flag = (doc.height()) <= (window.innerHeight + win.scrollTop());

   			if(!loading && flag) {
				loading =true;
 				setTimeout(function(){
					for(var i =0;i<6;i+=2){
						itemList.append(loadTpl("#tpl-item",{
							"title1":"title"+i,
							"title2":"title"+(i+1),
							"test":{
								"abc":"abc"
							}
						}));
					}
 					
					loading =false;
				},1500);
			}
		});
		home.unbind("touchstart touchmove touchend")
		.on('touchstart',function(e){
            if(loading){
                touchStartY = e.originalEvent.touches[0].pageY|| 0;
            }
        })
		.on('touchmove',function(e){
            if(!loading){
				return;
            }
 			if(playAnim){
				return;
			}
 			
			var currY = e.originalEvent.touches[0].pageY|| 0;
			var height = loadDom.height()+  Math.pow(Math.abs(currY-touchStartY),4);
			height = Math.min(maxHeight,height);
			loadDom.height(height);
        })
		.on('touchend',function(){
 			if(playAnim){
				return;
			}
			playAnim = true;
			loadDom.animate({height: oldHeight}, 1000,'linear',function(){
				playAnim = false;
			});
         });
		//shoppingCart
		//NAV_HISTORY
		NAV_HISTORY.register("product",function(ctx){
			this.productCtx = ctx | this.productCtx;
 			b.find(">div").hide();
			product.show();
		});
		
		///////////////////common/////////////////////////
		//tab
		$(".tabToolbar").find(">div").each(function(i,e){
			var d = $(e), 
				p = d.parent(),
				tagId = d.attr("tabRef"),
				groudId = "#"+p.attr("id"),
				ctx = p.attr("tabCtx")
				tClss = p.attr("tabTClass"),
				rClass = p.attr("tabRClass");
			d.click(function(){
				triggerTab(tagId,groudId,tClss,rClass);
			});
		});
		//checkBoxGroup
		$(".checkBoxGroup").click(function(){
			var $this =  $(this),
				flag = $this.prop('checked'),
				sub = $this.attr('refSub')
 			$("."+sub).each(function(){
				$(this).prop("checked",flag);
			});
		});
	});
})();

if (!String.prototype.format) {                                                 
  String.prototype.format = function() {                                        
	var obj = arguments[0];                                                                                       
	return this.replace(/\{\{((\w+\.?))+\}\}/g, function(match, item,c) {
		var arKeys = match.replace("{{","").replace("}}","").split(".");
		var ret = null;
		if(arKeys.length>1){
			ret = getMapSubValue(obj,arKeys,0);
		}else{
			ret = obj[item];
		}
	  return typeof  ret != 'undefined' ? ret: match;
	});
  };
} 

if (!Array.prototype.remove) {                                                 
  Array.prototype.remove = function(index) {    
	if(index>=this.length || index <0){
		return;
	}
	this.splice(index,1);                                                         
  };                                                                          
}
if (!Array.prototype.removeValue) {                                                 
	Array.prototype.removeValue = function(value) {    
		var i = this.indexOf(value);
		this.remove(i);
	};                                                                
} 
if (!Array.prototype.uniquePush) {                                                 
  Array.prototype.uniquePush = function(value) {    
		this.removeValue(value);
		this.push(value);
  };                              
} 
/**
t: current time（当前时间）；
b: beginning value（初始值）；
c: change in value（变化量）；
d: duration（持续时间
*/
var Tween = {
 Linear: function(t,b,c,d){ return c*t/d + b; },
 Quad: {
  easeIn: function(t,b,c,d){
   return c*(t/=d)*t + b;
  },
  easeOut: function(t,b,c,d){
   return -c *(t/=d)*(t-2) + b;
  },
  easeInOut: function(t,b,c,d){
   if ((t/=d/2) < 1) return c/2*t*t + b;
   return -c/2 * ((--t)*(t-2) - 1) + b;
  }
 },
 Cubic: {
  easeIn: function(t,b,c,d){
   return c*(t/=d)*t*t + b;
  },
  easeOut: function(t,b,c,d){
   return c*((t=t/d-1)*t*t + 1) + b;
  },
  easeInOut: function(t,b,c,d){
   if ((t/=d/2) < 1) return c/2*t*t*t + b;
   return c/2*((t-=2)*t*t + 2) + b;
  }
 },
 Quart: {
  easeIn: function(t,b,c,d){
   return c*(t/=d)*t*t*t + b;
  },
  easeOut: function(t,b,c,d){
   return -c * ((t=t/d-1)*t*t*t - 1) + b;
  },
  easeInOut: function(t,b,c,d){
   if ((t/=d/2) < 1) return c/2*t*t*t*t + b;
   return -c/2 * ((t-=2)*t*t*t - 2) + b;
  }
 },
 Quint: {
  easeIn: function(t,b,c,d){
   return c*(t/=d)*t*t*t*t + b;
  },
  easeOut: function(t,b,c,d){
   return c*((t=t/d-1)*t*t*t*t + 1) + b;
  },
  easeInOut: function(t,b,c,d){
   if ((t/=d/2) < 1) return c/2*t*t*t*t*t + b;
   return c/2*((t-=2)*t*t*t*t + 2) + b;
  }
 },
 Sine: {
  easeIn: function(t,b,c,d){
   return -c * Math.cos(t/d * (Math.PI/2)) + c + b;
  },
  easeOut: function(t,b,c,d){
   return c * Math.sin(t/d * (Math.PI/2)) + b;
  },
  easeInOut: function(t,b,c,d){
   return -c/2 * (Math.cos(Math.PI*t/d) - 1) + b;
  }
 },
 Expo: {
  easeIn: function(t,b,c,d){
   return (t==0) ? b : c * Math.pow(2, 10 * (t/d - 1)) + b;
  },
  easeOut: function(t,b,c,d){
   return (t==d) ? b+c : c * (-Math.pow(2, -10 * t/d) + 1) + b;
  },
  easeInOut: function(t,b,c,d){
   if (t==0) return b;
   if (t==d) return b+c;
   if ((t/=d/2) < 1) return c/2 * Math.pow(2, 10 * (t - 1)) + b;
   return c/2 * (-Math.pow(2, -10 * --t) + 2) + b;
  }
 },
 Circ: {
  easeIn: function(t,b,c,d){
   return -c * (Math.sqrt(1 - (t/=d)*t) - 1) + b;
  },
  easeOut: function(t,b,c,d){
   return c * Math.sqrt(1 - (t=t/d-1)*t) + b;
  },
  easeInOut: function(t,b,c,d){
   if ((t/=d/2) < 1) return -c/2 * (Math.sqrt(1 - t*t) - 1) + b;
   return c/2 * (Math.sqrt(1 - (t-=2)*t) + 1) + b;
  }
 },
 Elastic: {
  easeIn: function(t,b,c,d,a,p){
   if (t==0) return b;  if ((t/=d)==1) return b+c;  if (!p) p=d*.3;
   if (!a || a < Math.abs(c)) { a=c; var s=p/4; }
   else var s = p/(2*Math.PI) * Math.asin (c/a);
   return -(a*Math.pow(2,10*(t-=1)) * Math.sin( (t*d-s)*(2*Math.PI)/p )) + b;
  },
  easeOut: function(t,b,c,d,a,p){
   if (t==0) return b;  if ((t/=d)==1) return b+c;  if (!p) p=d*.3;
   if (!a || a < Math.abs(c)) { a=c; var s=p/4; }
   else var s = p/(2*Math.PI) * Math.asin (c/a);
   return (a*Math.pow(2,-10*t) * Math.sin( (t*d-s)*(2*Math.PI)/p ) + c + b);
  },
  easeInOut: function(t,b,c,d,a,p){
   if (t==0) return b;  if ((t/=d/2)==2) return b+c;  if (!p) p=d*(.3*1.5);
   if (!a || a < Math.abs(c)) { a=c; var s=p/4; }
   else var s = p/(2*Math.PI) * Math.asin (c/a);
   if (t < 1) return -.5*(a*Math.pow(2,10*(t-=1)) * Math.sin( (t*d-s)*(2*Math.PI)/p )) + b;
   return a*Math.pow(2,-10*(t-=1)) * Math.sin( (t*d-s)*(2*Math.PI)/p )*.5 + c + b;
  }
 },
 Back: {
  easeIn: function(t,b,c,d,s){
   if (s == undefined) s = 1.70158;
   return c*(t/=d)*t*((s+1)*t - s) + b;
  },
  easeOut: function(t,b,c,d,s){
   if (s == undefined) s = 1.70158;
   return c*((t=t/d-1)*t*((s+1)*t + s) + 1) + b;
  },
  easeInOut: function(t,b,c,d,s){
   if (s == undefined) s = 1.70158; 
   if ((t/=d/2) < 1) return c/2*(t*t*(((s*=(1.525))+1)*t - s)) + b;
   return c/2*((t-=2)*t*(((s*=(1.525))+1)*t + s) + 2) + b;
  }
 },
 Bounce: {
  easeIn: function(t,b,c,d){
   return c - Tween.Bounce.easeOut(d-t, 0, c, d) + b;
  },
  easeOut: function(t,b,c,d){
   if ((t/=d) < (1/2.75)) {
    return c*(7.5625*t*t) + b;
   } else if (t < (2/2.75)) {
    return c*(7.5625*(t-=(1.5/2.75))*t + .75) + b;
   } else if (t < (2.5/2.75)) {
    return c*(7.5625*(t-=(2.25/2.75))*t + .9375) + b;
   } else {
    return c*(7.5625*(t-=(2.625/2.75))*t + .984375) + b;
   }
  },
  easeInOut: function(t,b,c,d){
   if (t < d/2) return Tween.Bounce.easeIn(t*2, 0, c, d) * .5 + b;
   else return Tween.Bounce.easeOut(t*2-d, 0, c, d) * .5 + c*.5 + b;
  }
 }
}


var NAV_HISTORY = {
	pageCb : {},
	pageLink :[],
	max : 8,
	register:function(id,recoverCallBack){
		if(!this.pageCb[id] && recoverCallBack){
			this.pageCb[id] = recoverCallBack;
 		}
	},
	push:function(id,recoverCallBack){
		this.register(id,recoverCallBack);
		this.pageLink.uniquePush(id);
 		if(this.pageLink.length>this.max){
			this.pageLink.shift();
		}
	},
	pop:function(ctx){
		if(this.pageLink >0){
			var id = this.pageLink.pop();
			var rcb = this.pageCb[id];
			if(rcb){
				rcb(ctx);
			}
		}
	},
	jumpPage:function(curr,ctx){
 		var rcb = this.pageCb[curr];
		if(rcb){
			rcb(ctx);
		}
		this.push(curr);
	}
};

var TPL_DATA ={};
function loadTpl(id,obj){
	var tpl = TPL_DATA[id];
	if(!tpl){
		tpl = $(id).html();
		TPL_DATA[id] = tpl;
	}
	if(obj){
		return tpl.format(obj);
	}
	return tpl;
}

function triggerTab(targetId,groupId,tClss,rClass){
 	$(groupId).find(">div").each(function(i,e){
 		var d = $(e),
			itemId = d.attr("tabRef"),
			itemDom = $("#"+itemId);
		d.removeClass(tClss);
		d.removeClass(rClass);
		itemDom.hide();
		if(itemId==targetId){
			d.addClass(tClss);
			itemDom.show();
			//NAV_HISTORY.push(targetId);
 		}else{
			d.addClass(rClass);
		}
	});
}

function getMapSubValue(map,arKey,os){
	map = map[arKey[os]];
	if(typeof map == 'undefined'){
		return null;
	}
	os++;
	if(arKey.length==os){
		return map;
	}
	return getMapSubValue(map,arKey,os++);
}
function registerMVVM(obj,key,id){
	Object.defineProperty(obj, key, {
		get: function(){
			return $(id).html();
		},
		set: function(v){
			$(id).html(v);
		}
	});
}