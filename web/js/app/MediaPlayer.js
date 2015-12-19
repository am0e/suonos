var MediaPlayer = function() {
   // Get the audio.
   //
   this.audio = $('#audio')[0];
   this.playhead = $('#audioPlayer .playhead')[0];
   this.timeline = $('#audioPlayer .timeline')[0];
   this.timelineWidth = this.timeline.offsetWidth - this.playhead.offsetWidth - 1;
   this.playBtn = $('#audioPlayer .playBtn');
   this.timeinfo = $('#audioPlayer .timeinfo');
   
   // PlayMode:
   // local	Play on browser.
   // remote	Play on Suonos server.
   //
   this.playMode = "local";

   // Check if we are running on server.
   //
   if (typeof suonosRequest == 'function') {
      this.playMode = "remote";
   }

   this.playlist = [];
   this.playlistPos = 0;
   this.playlistRepeat = true;
   this.trackDuration = 0;
   
   var $this = this;
   var onplayhead = false;
   
   this.audio.onended = function(e) {
      $this.trackDuration = 0;
      $this.nextTrack();
   };
   
   this.audio.onplay = function() {
      $this.playBtn.removeClass("fa-play");
      $this.playBtn.addClass("fa-pause");
   }
   
   this.audio.onpause = function() {
      $this.playBtn.removeClass("fa-pause");
      $this.playBtn.addClass("fa-play");
   }
   
   // Get the audio file duration
   //
   this.audio.oncanplaythrough = function() {
      $this.trackDuration = self.audio.duration;
      //console.log($this.trackDuration);
   };
   
   this.audio.ontimeupdate = function() {
      if (!onplayhead) {
	 var pos = $this.timelineWidth * ($this.audio.currentTime / $this.trackDuration);
	 if (pos < 1) {
	    pos = 1;
	 }
	 $this.playhead.style.marginLeft = pos + "px";
	 
	 $this.timeinfo.text(formatTime($this.audio.currentTime));
      }
   };

   var formatTime = function(ts) {
      var m = Math.floor(ts / 60);
      m = (m >= 10) ? m : "" + m;
      var s = Math.floor(ts % 60);
      s = (s >= 10) ? s : "0" + s;
      return m + ":" + s;
   };
   
   var clickPercent = function(e) {
	return (e.pageX - $this.timeline.offsetLeft) / $this.timelineWidth;
   }
   
   var moveplayhead = function(e) {
      var x = e.pageX;
      x -= $this.playhead.offsetWidth / 2;
      
      var newMargLeft = x - $this.timeline.offsetLeft;
	
      if (newMargLeft >= 0 && newMargLeft <= $this.timelineWidth) {
	 $this.playhead.style.marginLeft = newMargLeft + "px";
      }
      if (newMargLeft < 1) {
	 $this.playhead.style.marginLeft = "1px";
      }
      if (newMargLeft > $this.timelineWidth) {
	 $this.playhead.style.marginLeft = $this.timelineWidth + "px";
      }

      if ($this.trackDuration) {
	 var val = $this.trackDuration * clickPercent(e);
	
	 //console.log("Set currentTime to " + val);
	 $this.audio.currentTime = val;
      }
      
      // Prevent default processing to suppress text selection during mouse drag.
      //
      if(e.preventDefault) e.preventDefault();
      return false;
   };


   this.timeline.addEventListener("click", moveplayhead);

   // Draggable.
   //
   this.playhead.addEventListener('mousedown', function(e) {
      onplayhead = true;
      window.addEventListener('mousemove', moveplayhead, true);
      
      // Prevent default processing to suppress text selection during mouse drag.
      //
      if(e.preventDefault) e.preventDefault();
      return false;
   });
   
   window.addEventListener('mouseup', function(e) {
      if (onplayhead == true) {
	 onplayhead = false;
	 window.removeEventListener('mousemove', moveplayhead, true);
	 moveplayhead(e);
	 
	 if ($this.trackDuration) {
	    // update audio.
	    //
	    audio.currentTime = $this.trackDuration * clickPercent(e);
	 }
      }
   });
   
   console.log("PlayMode " + this.playMode);
}


var Audio = function() {
   
   // Get the audio.
   //
   this.audio = $('#audio');
   this.playMode = "";
}

MediaPlayer.prototype.play = function() {
   this.playlistPos = -1;
   this.nextTrack();
}

MediaPlayer.prototype.nextTrack = function() {
   this.playlistPos++;
   
   // Check for auto repeat.
   //
   if (this.playlistRepeat && this.playlistPos >= this.playlist.length) {
      this.playlistPos = 0;
   }
   
   // Check if we can play the next track.
   //
   if(this.playlistPos < this.playlist.length) {
      var track = this.playlist[this.playlistPos];
      
      // Build url to track.
      //
      var url = `/ws/tracks/-${track.id}/download`; 
      
      console.log("MediaPlayer.PlayTrack " + this.playlistPos + " " + url + " " + track.title);

      // Load and play the track. This will abort any existing track that is playing.
      //
      this.audio.src = url;
      this.audio.load();
      this.audio.play();
   } else {
      console.log("Finished");
   }
}


MediaPlayer.prototype.playObject = function(o) {
   if (o.type=="MusicAlbum") {
      this.playlist = [];
      
      for (id in o.tracks) {
         this.playlist.push(o.tracks[id]);
      }
      
      this.play();
      
   } else if (o.type=="MusicTrack") {
      this.playlist = [ o ];
      this.play();
      
   } else {
      alert("TODO: Error message");
   }
}


MediaPlayer.prototype.playPauseBtn = function() {
   if (this.audio.paused) {
      this.audio.play();
   } else if (!this.audio.ended) {
      this.audio.pause();
   }
}

MediaPlayer.prototype.forwardBtn = function() {
   this.nextTrack();
}

MediaPlayer.prototype.backBtn = function() {
   if (this.playlistPos > 0) {
      this.playlistPos -= 2;
      this.nextTrack();
   }
}