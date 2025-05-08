<!DOCTYPE html>
<html>
<head>
<meta http-equiv = "Content-Type" content="text/html; charset=utf-8">
    <meta name="Generator" content="Freemarker">
        <style>
        body {
background-color: #FBE4D5;
font-family: 'Monotype Corsiva', sans-serif;
font-size: 22pt;
text-align: center;
margin: 0;
padding: 0;
}
#plain_text1,#plain_text2,#plain_text3,#plain_text4,#plain_text5,#plain_text6 {
color: #993300;
}
#place_holder1,#place_holder2,#place_holder3,#place_holder4{
color: #002060;
}
</style>
</head>
<body>
<div class="content">
        <p style="font-weight:bold;"><span id="plain_text1">Congratulations to </span><strong id="place_holder1">${employeeName}</strong><span id="plain_text2"> on your </span><strong id="place_holder2">${yearsWorked}${anniversarySuffix}</strong><span id="plain_text3"> Work Anniversary at </span><strong id="place_holder3">${departmentName}</strong><span id="plain_text6"> Team!</span></p>
        <p id="plain_text4">We thank you for your hard work, dedication, and commitment over the <span id="place_holder4">${yearsWorked}</span> years.</p>
        <p id="plain_text5">Wishing you many more successful years ahead!</p>
        <img src="cid:anniversaryImage" class="image" alt="Anniversary Image"/>
        <p id="footer" style="color: #993300;" class="greeting">
            <strong>Regards</strong>,<br><span style="color: #002060;"><strong>Direction Software LLP</strong></span>
        </p>
    </div>
</body>
</html>
