
    ______ ______                                  __            _  __     __
   / ____// ____/____ ___   ____   ___   ____ _   / /_   __  __ (_)/ /____/ /
  / /_   / /_   / __ `__ \ / __ \ / _ \ / __ `/  / __ \ / / / // // // __  / 
 / __/  / __/  / / / / / // /_/ //  __// /_/ /  / /_/ // /_/ // // // /_/ /  
/_/    /_/    /_/ /_/ /_// .___/ \___/ \__, /  /_.___/ \__,_//_//_/ \__,_/   
                        /_/           /____/                                 


                build: ffmpeg-2.8.1-64bit-static.tar.xz
              version: 2.8.1
 
                  gcc: 4.9.3
                 yasm: 1.3.0

               libass: 0.13.0
               libvpx: 1.4.0-1523-gc2b8b5b
              libx264: 0.148.209 7599210
              libx265: 1.8+31-b6156a08b1de
              libxvid: 1.3.4-1
              libwebp: 0.4.3 
            libgnutls: 3.3.16
            libtheora: 1.1.1
            libfrei0r: 1.4-3+b1
           libvidstab: 1.10
          libfreetype: 2.6-2
          libopenjpeg: 1.5.2 

              libsoxr: 0.1.2
              libopus: 1.1-2
             libspeex: 1.2
            libvorbis: 1.3.4-3
           libmp3lame: 3.99.5
         libvo-aacenc: 0.1.3-1
       libvo-amrwbenc: 0.1.3-1
    libopencore-amrnb: 0.1.3-2.1
    libopencore-amrwb: 0.1.3-2.1

      For HEVC/H.265 encoding:  ffmpeg -h encoder=libx265
                                http://x265.readthedocs.org/en/default/cli.html#standalone-executable-options

      For AVC/H.264 encoding:   ffmpeg -h encoder=libx264
                                http://mewiki.project357.com/wiki/X264_Settings

                 FFmpeg Wiki:   https://trac.ffmpeg.org/wiki


      Notes: ffmpeg-10bit has support for AVC/H.264, HEVC/H.265, and VP9 high bit depth encoding.
             However, the 32bit builds lack HEVC/H.265 high bit depth because it's not supported by
             libx265.

             A limitation of statically linking glibc is the loss of DNS resolution. Installing
             nscd through your package manager will fix this or you can use
             "ffmpeg -i http://<ip address here>/" instead of "ffmpeg -i http://example.com/"


      If you appreciate this up-to-date build of FFmpeg and my time that goes into to maintaining it,
      please consider making a donation. Thank you.

      
      Paypal tinyurl: http://goo.gl/1Ol8N
      Bitcoin: 18aREiQcTTem7uJLSYSdyu6bk8tMncZruk

      email: john.vansickle@gmail.com
      irc: relaxed @ irc://irc.freenode.net #ffmpeg
      url: http://johnvansickle.com/ffmpeg/
