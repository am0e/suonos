
function init() {
   for (var i in appData.bindList) {
      var s = appData.bindList[i];
      var it = appData;
      var p = s.split('.');
      for (var j in p) {
         var pa = p[j];
         it = it[pa];
      }
   
      for (var j=0; j!=it.length; j+=1) {
         var pa = it[j];
         it[j] = appData.desc[pa.substring(1)];
      }
   }
}

init();

/**
 * https://scotch.io/tutorials/single-page-apps-with-angularjs-routing-and-templating
 * 
 * http://coder1.com/articles/consuming-rest-services-angularjs
 */
var suonosApp = angular.module('suonosApp', ['ngAnimate', 'ngRoute']);

suonosApp.config( function( $routeProvider ) {
   $routeProvider
   .when("/music", {controller: "DefaultCtrl", templateUrl: "/res/views/music/index.html"})
   .when("/music/az/:id/:qt", {controller: "MusicAzCtrl", templateUrl: "/res/views/music/az.html"})
   .when("/music/az/:id", {controller: "MusicAzCtrl", templateUrl: "/res/views/music/az.html"})
   .when("/music/albums", {controller: "MusicAlbumsCtrl", templateUrl: "/res/views/music/albums.html"})
   .when("/music/albums/:id", {controller: "MusicAlbumCtrl", templateUrl: "/res/views/music/album.html"})
   .when("/videos", {controller: "DefaultCtrl", templateUrl: "/res/views/videos/index.html"})
   .when("/videos/query", {controller: "DefaultCtrl", templateUrl: "/res/views/videos/query.html"})
   .when("/home", {controller: "DefaultCtrl", templateUrl: "/res/views/home.html"})
   .when("/mediaplayer", {controller: "MediaPlayerCtrl", templateUrl: "/res/views/mediaplayer.html"})
   .when("/search", {controller: "SearchCtrl", templateUrl: "/res/views/search.html"})
   .otherwise({ redirectTo: "/login" });
});


/**
 * Frame ctrl for the app. Only called once.
 */
suonosApp.controller('FrameCtrl', function($scope) {
   $scope.appData = appData;
   $scope.mediaPlayer = suonosApp.mediaPlayer;
});

suonosApp.controller('Search', function($scope, $location) {
   var timer = null;
   
   $scope.searchSubmit = function() {
      window.clearTimeout(timer);
      timer = null;
      
      // Redirect to search view.
      //
      if ($scope.q) {
         $location.url("/search?q=" + encodeURIComponent($scope.q));
      }
      
      return true;
   };
   
   $scope.searchChange = function() {
      // Clear existing timer.
      //
      window.clearTimeout(timer);
      timer = null;
      
      // Do request when stopped typing.
      //
      timer = window.setTimeout( function() {
	 var val = $scope.q;
	 if (val) {
	    console.log(val);

	    aja()
	    	.url('/ws/search/suggestions')
	    	.queryString( {"q": val } )
	    	.on('success', function(data) {
	    	   console.log("data");
	    	}
	    ).go();
	 }
      }, 1000 );
   }
});


//create the controllers

suonosApp.controller('DefaultCtrl', function($scope) {
});

suonosApp.controller('MusicCtrl', function($scope, $routeParams, $location, $http) {
});

suonosApp.controller('SearchCtrl', function($scope, $routeParams, $location, $http) {
   
   var q = $routeParams.q;
   
   var dataUrl = replaceAll("/ws/search?kq={}&target=list", "{}", encodeURIComponent(q));
   
   console.log(dataUrl);
   
   $http.get(dataUrl).success(function(resp) {
      console.log("search data results");
      $scope.resp = resp;
   });
});


/**
 * Controller for Albums [A-Z], Artists [A-Z], etc.
 */
suonosApp.controller('MusicAzCtrl', function($scope, $routeParams, $location, $http) {
   // id. Eg "artists"
   //
   var id = $routeParams.id;
   var qt = $routeParams.qt;
   var descId = "music_az_" + id + (qt ? "_f": "");
   initScope($scope, $routeParams, $location, $http, descId);
});

/**
 * Controller for Albums [A-Z], Artists [A-Z], etc.
 */
suonosApp.controller('MusicAlbumsCtrl', function($scope, $routeParams, $location, $http) {
   $routeParams.id = "albums";
   initScope($scope, $routeParams, $location, $http, "music_albums");
});


/**
 * Controller for Album 
 */
suonosApp.controller('MusicAlbumCtrl', function($scope, $routeParams, $location, $http) {
   // Get data.
   //
   var id = $routeParams.id;

   // Get data including tracks.
   //
   var dataUrl = replaceAll("/ws/albums/-{id}?include=tracks", "{id}", id);
   var breadcrumb = [];
   
   breadcrumb.push( {
      text: "Music",
      href: "#/music"
   });

   breadcrumb.push( {
      text: "Albums",
      href: "#/music/albums"
   });

   $scope.breadcrumb = breadcrumb;
   
   $http.get(dataUrl).success(function(resp) {
      var album = resp.data;
      $scope.tags = {};
      $scope.tags.artists = formatTag(album.tags.artists);
      $scope.tags.composers = formatTag(album.tags.composers);
      $scope.tags.genres = formatTag(album.tags.genres);
      $scope.tags.year = formatTag(album.tags.year);
      $scope.album = album;
      
      breadcrumb.push( {
	 text: album.title,
	 href: "#/music/az/albums" 
      });
   });
});


suonosApp.controller('VideosCtrl', function($scope) {
});


suonosApp.controller('MediaPlayerCtrl', function($scope, $routeParams, $location, $http) {
});

suonosApp.filter("enc", function() {
   return function(p) {
      return encodeURIComponent(p);
   }
} );

suonosApp.filter("qt", function() {
   return function(p) {
      return encodeURIComponent(p);
   }
} );

suonosApp.filter("tag", function() {
   return function(v) {
      return formatTag(v);
   }
} );


/**
 * Format 185 as 02:05
 */
suonosApp.filter("timelen", function() {
   return function(v) {
      var tm = secondsToTime(v);
      var s = "";
      
      if (tm.h) {
	 s = ((tm.h < 10) ? '0' + tm.h : tm.h) + ":";
      }
      
      s = s + ((tm.m < 10) ? '0' + tm.m : tm.m) + ":";
      s = s + ((tm.s < 10) ? '0' + tm.s : tm.s);
      
      return s;
   }
} );


suonosApp.directive('queryItem', function() {
   return {
     restrict: 'E',
     templateUrl: '/res/views/directives/query-item.html'
   };
} );

suonosApp.directive('taglink', function() {
   return {
      restrict: 'E',
      scope: { id: '=', p: '=' },
      transclude: false,
      template: function(elem,attrs,$scope) {
	 var s = "<a href=#/music/az/{{id}}/{{p|enc}}>{{p}}</a>";
	 return s;
      }
   };
} );

suonosApp.directive('taglist', function() {
   return {
      restrict: 'E',
      transclude: false,
      scope: { data: '=', qf: '=' },
      templateUrl: "/res/views/directives/taglist.html" 
   };
} );

suonosApp.directive('album', function() {
   return {
      templateUrl: '/res/views/directives/album.html',
      restrict: 'E',
      scope: { item: '=item' },	
      controller: function($scope) {
      }
   };
} );


function replaceAll(s, target, replacement) {
   return s.split(target).join(replacement);
}

function formatTag(tag) {
   if (!tag) {
      return null;
   }
   
   if (tag.length==1) {
      return tag[0];
   }

   if (tag.length==2) {
      return tag[0] + ", " + tag[1];
   }
   
   return null;
}

function secondsToTime(secs) {
    secs = Math.round(secs);
    var hours = Math.floor(secs / (60 * 60));

    var divisor_for_minutes = secs % (60 * 60);
    var minutes = Math.floor(divisor_for_minutes / 60);

    var divisor_for_seconds = divisor_for_minutes % 60;
    var seconds = Math.ceil(divisor_for_seconds);

    var obj = {
        "h": hours,
        "m": minutes,
        "s": seconds
    };
    return obj;
}



function initScope($scope, $routeParams, $location, $http, descId) {

   // param. Eg "A"
   //
   var qf = $routeParams.qf;
   var qt = $routeParams.qt;
   var id = $routeParams.id;
   var ft = $routeParams.ft;
   var te = $routeParams.te;
   var q  = $routeParams.q;
   var s = $routeParams.s;
   
   if (qf && !ft) {
      // Look up field ft for qf.
      // eg album_artists_u -> Artists
      //
      ft = appData.qfmap[qf].ft;
   }
   
   if (q=='1') {
      // Make the lucene query string.
      // qf:"te"
      //
      q = qf + replaceAll(':"{}"', "{}", te);
   } else if (qt) {
      q = qt;
   } else {
      q = "";
      $scope.AtoZ = 1;
   }
   
   // Filter "music_az_artists_f" for /artists/B
   //
   var desc = appData.desc[descId];
   
   $scope.desc = desc;
   
   $scope.href = function(it) {
      var s = desc.href;
      s = replaceAll(s, "{id}", id);
      if (it=='*') {
	 if (desc.href_all) {
	    s = desc.href_all;
	 } else {
	    s = replaceAll(s, "{qt}", "all");
	 }
      } else {
	 s = replaceAll(s, "{te}", encodeURIComponent(it.term));
	 s = replaceAll(s, "{qt}", encodeURIComponent(it.queryTerm));
      }
      return s;
   };

   // Replace the [q] param.
   //
   var dataUrl = "/ws" + replaceAll(desc.query, "{q}", encodeURIComponent(q));
   
   // Get data.
   //
   $http.get(dataUrl).success(function(resp) {
      $scope.resp = resp;
   });
   
   var breadcrumb = [];
   
   breadcrumb.push( {
      text: "Music",
      href: "#/music"
   });

   breadcrumb.push( {
      text: desc.title,
      href: "#/music/az/" + id 
   });

   if (te) {
      breadcrumb.push( {
	 text: ft + ": " + te.charAt(0).toUpperCase() + te.slice(1),
      });
      
   } else if (q) {
      breadcrumb.push( {
	 text: qt.charAt(0).toUpperCase() + qt.slice(1),
	 href: "#/music/az/" + id + "/" + encodeURIComponent(q)
      });
   }
   
   $scope.breadcrumb = breadcrumb;
}

suonosApp.mediaPlayer = new MediaPlayer();
suonosApp.searchBox = new SearchBox(suonosApp);
