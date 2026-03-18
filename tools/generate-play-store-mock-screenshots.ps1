Add-Type -AssemblyName System.Drawing

$outDir = Join-Path $PSScriptRoot "..\docs\play-store-assets\mock"
New-Item -ItemType Directory -Force -Path $outDir | Out-Null

$C = @{ Navy='#002D74'; Blue='#1A73E8'; Bg='#F6F8FB'; Surface='#FFFFFF'; Text='#202124'; Muted='#5F6368'; Line='#DADCE0'; Gold='#D4A017' }
function Color($hex){ [System.Drawing.ColorTranslator]::FromHtml($hex) }
function Brush($hex){ New-Object System.Drawing.SolidBrush (Color $hex) }
function Pen($hex,$w=1){ New-Object System.Drawing.Pen ((Color $hex),$w) }
function FontObj($size,$style='Regular'){ New-Object System.Drawing.Font('Segoe UI',$size,[System.Drawing.FontStyle]::$style) }
function Txt($g,$text,$size,$hex,$x,$y,$style='Regular'){ $g.DrawString($text,(FontObj $size $style),(Brush $hex),$x,$y) }
function Box($g,$hex,$x,$y,$w,$h){ $g.FillRectangle((Brush $hex),$x,$y,$w,$h) }
function Outline($g,$hex,$x,$y,$w,$h,$pw=1){ $g.DrawRectangle((Pen $hex $pw),$x,$y,$w,$h) }
function New-Canvas(){ $bmp=New-Object System.Drawing.Bitmap 1080,1920; $g=[System.Drawing.Graphics]::FromImage($bmp); $g.SmoothingMode='AntiAlias'; $g.TextRenderingHint='AntiAliasGridFit'; $g.Clear((Color $C.Bg)); Box $g $C.Navy 0 0 1080 250; @{bmp=$bmp; g=$g} }
function Draw-Phone($g,$title,$subtitle){ Box $g $C.Surface 120 300 840 1450; Outline $g $C.Line 120 300 840 1450 2; Box $g $C.Surface 150 340 780 60; Txt $g '9:41' 15 $C.Text 170 355 'Bold'; Txt $g 'LTE   100%' 15 $C.Text 770 355; Txt $g 'TSIN Learning' 18 $C.Text 180 430 'Bold'; Txt $g $title 42 '#FFFFFF' 76 60 'Bold'; Txt $g $subtitle 20 '#D6E3FF' 76 125; Txt $g 'MOCK SCREENSHOT  TEST ASSET' 16 $C.Gold 76 185 'Bold' }
function Draw-BottomNav($g,$selected){ Box $g '#FBFCFE' 150 1640 780 80; Outline $g $C.Line 150 1640 780 80 1; $items=@('Home','Schedule','Alerts','Settings'); for($i=0;$i -lt $items.Count;$i++){ $x=205+($i*185); $color=if($items[$i]-eq $selected){$C.Navy}else{$C.Muted}; Txt $g '' 20 $color $x 1650; Txt $g $items[$i] 14 $color ($x-16) 1680 } }
function Card($g,$title,$x,$y,$w,$h){ Box $g $C.Surface $x $y $w $h; Outline $g $C.Line $x $y $w $h 1; if($title){ Txt $g $title 18 $C.Text ($x+22) ($y+18) 'Bold' } }
function Save-Screen($name,$draw){ $c=New-Canvas; $bmp=$c.bmp; $g=$c.g; & $draw $g; $path=Join-Path $outDir $name; $bmp.Save($path,[System.Drawing.Imaging.ImageFormat]::Png); $g.Dispose(); $bmp.Dispose(); Write-Host "Generated $path" }

Save-Screen '01-login-mock.png' { param($g)
  Draw-Phone $g 'Secure sign in' 'Sign in with your TSIN account and MFA'
  Txt $g 'TSIN Learning' 24 $C.Text 405 520 'Bold'; Txt $g 'Sign in to your account' 16 $C.Muted 405 560
  Card $g 'Email' 200 640 680 86; Txt $g 'learner@example.com' 16 $C.Muted 222 682
  Card $g 'Password' 200 748 680 86; Txt $g '' 16 $C.Muted 222 790
  Box $g $C.Navy 200 895 680 70; Txt $g 'Sign in' 18 '#FFFFFF' 495 918 'Bold'
  Card $g 'Account security' 200 1010 680 150; Txt $g ' Multi-factor authentication supported' 15 $C.Text 222 1060; Txt $g ' Automatic session refresh' 15 $C.Text 222 1095; Txt $g ' Secure learner access' 15 $C.Text 222 1130 }
Save-Screen '02-home-mock.png' { param($g)
  Draw-Phone $g 'Track progress fast' 'Your learning overview in one place'
  Txt $g 'Welcome, Learner' 26 $C.Text 190 520 'Bold'; Txt $g "Here's your learning overview" 16 $C.Muted 190 562
  Card $g 'Program Overview' 180 630 720 220; Txt $g 'Clinical Skills Assessment Program' 18 $C.Text 202 680 'Bold'; Txt $g 'Status: Active' 15 $C.Muted 202 720; Txt $g 'Progress: 8/12 sessions' 15 $C.Muted 202 755; Box $g '#D6E3FF' 202 792 430 16; Box $g $C.Navy 202 792 285 16
  Card $g 'Next Session' 180 880 720 210; Txt $g 'OSCE Practice Session' 18 $C.Text 202 930 'Bold'; Txt $g 'Thu, Mar 21  9:00 AM - 12:00 PM' 15 $C.Muted 202 970; Txt $g 'Touchstone Institute, Toronto' 15 $C.Muted 202 1005
  Card $g 'Why learners use the app' 180 1120 720 220; Txt $g ' Check upcoming sessions quickly' 15 $C.Text 202 1170; Txt $g ' See program status and progress' 15 $C.Text 202 1205; Txt $g ' Stay informed with reminders' 15 $C.Text 202 1240; Draw-BottomNav $g 'Home' }
Save-Screen '03-schedule-mock.png' { param($g)
  Draw-Phone $g 'Never miss a session' 'View your weekly schedule at a glance'
  Txt $g 'Schedule' 24 $C.Text 190 520 'Bold'; Txt $g 'Week of March 18, 2026' 16 $C.Muted 190 560
  Card $g 'Monday  Mar 18' 180 620 720 190; Txt $g 'Orientation Session' 17 $C.Text 202 675 'Bold'; Txt $g '9:00 AM - 11:00 AM' 15 $C.Muted 202 710; Txt $g 'Main Campus  Room 204' 15 $C.Muted 202 745
  Card $g 'Tuesday  Mar 19' 180 835 720 215; Txt $g 'Clinical Practice Lab' 17 $C.Text 202 890 'Bold'; Txt $g '1:00 PM - 4:00 PM' 15 $C.Muted 202 925; Txt $g 'Simulation Centre' 15 $C.Muted 202 960; Txt $g 'Case Review Seminar' 17 $C.Text 202 995 'Bold'; Txt $g '4:30 PM - 6:00 PM' 15 $C.Muted 202 1030
  Card $g 'Wednesday  Mar 20' 180 1075 720 170; Txt $g 'No sessions' 17 $C.Muted 202 1135; Card $g 'Instant updates' 180 1270 720 150; Txt $g 'Schedule changes can be sent by push notification.' 15 $C.Text 202 1325; Draw-BottomNav $g 'Schedule' }
Save-Screen '04-settings-mock.png' { param($g)
  Draw-Phone $g 'Choose the alerts you want' 'Control reminders and account settings'
  Txt $g 'Settings' 24 $C.Text 190 520 'Bold'; Card $g 'Account' 180 600 720 180; Txt $g 'Learner Name' 18 $C.Text 202 655 'Bold'; Txt $g 'learner@example.com' 15 $C.Muted 202 690; Txt $g 'Role: Learner' 15 $C.Muted 202 725
  Card $g 'Notifications' 180 810 720 420; Txt $g 'Push Notifications' 17 $C.Text 202 870 'Bold'; Txt $g 'Receive push notifications on this device' 14 $C.Muted 202 905; Box $g $C.Blue 760 870 90 36; Txt $g 'ON' 13 '#FFFFFF' 792 879 'Bold'
  Txt $g 'Schedule Reminders' 17 $C.Text 202 980 'Bold'; Txt $g 'Get reminders before upcoming sessions' 14 $C.Muted 202 1015; Box $g $C.Blue 760 980 90 36; Txt $g 'ON' 13 '#FFFFFF' 792 989 'Bold'
  Txt $g 'Orientation Reminders' 17 $C.Text 202 1090 'Bold'; Txt $g 'Get reminders for orientation events' 14 $C.Muted 202 1125; Box $g '#E8EAED' 760 1090 90 36; Txt $g 'OFF' 13 $C.Text 786 1099 'Bold'; Card $g 'App version' 180 1260 720 120; Txt $g 'Version 1.0.0' 16 $C.Muted 202 1312; Draw-BottomNav $g 'Settings' }
Save-Screen '05-mfa-mock.png' { param($g)
  Draw-Phone $g 'Extra protection with MFA' 'Verify your account securely before entering'
  Txt $g 'Verification code' 24 $C.Text 365 560 'Bold'; Txt $g 'Enter the 6-digit code from your app or email' 16 $C.Muted 280 600
  for($i=0;$i -lt 6;$i++){ Card $g '' (220+($i*105)) 700 80 90; Txt $g ($i+1) 22 $C.Text (250+($i*105)) 730 'Bold' }
  Box $g $C.Navy 220 840 640 70; Txt $g 'Verify and continue' 18 '#FFFFFF' 430 863 'Bold'; Card $g 'Why it matters' 220 960 640 180; Txt $g ' Helps protect learner accounts' 15 $C.Text 242 1015; Txt $g ' Supports app and email MFA' 15 $C.Text 242 1050; Txt $g ' Required before mobile access' 15 $C.Text 242 1085 }

$feature=New-Object System.Drawing.Bitmap 1024,500; $g=[System.Drawing.Graphics]::FromImage($feature); $g.SmoothingMode='AntiAlias'; $g.TextRenderingHint='AntiAliasGridFit'; $g.Clear((Color $C.Navy))
Box $g '#0B3B8C' 36 36 420 428; Box $g $C.Surface 520 60 420 380; Outline $g $C.Line 520 60 420 380 2
Txt $g 'TSIN Learning Companion' 34 '#FFFFFF' 64 92 'Bold'; Txt $g 'Schedules, reminders, and secure learner access' 18 '#D6E3FF' 64 150
Txt $g ' Personalized weekly schedule' 18 '#FFFFFF' 64 230; Txt $g ' Push reminders and updates' 18 '#FFFFFF' 64 268; Txt $g ' Program progress overview' 18 '#FFFFFF' 64 306; Txt $g ' Secure MFA sign-in' 18 '#FFFFFF' 64 344
Txt $g 'Welcome, Learner' 20 $C.Text 550 110 'Bold'; Txt $g 'Clinical Skills Assessment Program' 16 $C.Muted 550 150; Box $g '#D6E3FF' 550 205 280 14; Box $g $C.Navy 550 205 180 14
Card $g 'Next Session' 550 245 360 125; Txt $g 'OSCE Practice Session' 16 $C.Text 572 292 'Bold'; Txt $g 'Thu, Mar 21  9:00 AM' 14 $C.Muted 572 325
$feature.Save((Join-Path $outDir 'feature-graphic-mock.png'),[System.Drawing.Imaging.ImageFormat]::Png); $g.Dispose(); $feature.Dispose(); Write-Host 'Generated feature graphic'
