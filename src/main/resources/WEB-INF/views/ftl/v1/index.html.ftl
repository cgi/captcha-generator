<#ftl encoding='UTF-8'>
<html>
<head>
    <title>v1 - Тестовая страница</title>
    <!--meta charset="UTF-8"-->
</head>
<body>
    <h1>Тестовая страница для капчи - версия 1</h1>
    <img src="captcha" width="200px" height="64px" >
    <form action="captcha/ver" method="POST">
        Код: <input type="text" name="code" size="10">
        <input type="submit" name="Проверить">
    </form>
</body>
</html>