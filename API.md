<!-- open in IDE -->


<span style="font-size: 30px !important; color: yellow"> API </span>
<br><span style="font-size: 25px !important; color: OrangeRed"> JSON convention ISO 8601 </span></br>

<span style="font-size: 20px !important; color: Cornsilk"> availableChar([\w\s\_\-\.\,\:\@\#\!\?\*\<\>\&\~\`\|\/\+\=]) </span> 



<br><span style="font-size: 18px !important; color: green"> RouteGroup(users/register) </span></br>
<span style="color: red"> POST </span>
<br><span style="color: Fuchsia; font-size: 16px"> Request </span></br>

```json
[
    "userName" : (value: "String", required: true, length(5, 20), availableChar),
    "phoneNumber" : (value: "String", required: true, format(+7XXXXXXXXXX)),
    "password" : (value: "String", required: true, length(8, 64), availableChar)
    "name" : (value: "String", required: true, length(1, 20), availableChar),
    "surname" : (value: "String", required: true, length(1, 20), availableChar),
    "email" : (value: "String", required = false, default = null, EmailValid),
    "status" : (value: "String", required: false, default = null, length(1, 120), availableChar)
]
```
<span style="color: Fuchsia; font-size: 16px"> Response </span>

```json
Cookie:
accessToken
refreshToken
```

<br><span style="font-size: 18px !important; color: green"> RouteGroup(users/login) </span></br>
<span style="color: red"> POST </span>
<br><span style="color: Fuchsia; font-size: 16px"> Request </span></br>

```json
[
    "phoneNumber" : (value: "String", required: true, format(+7XXXXXXXXXX)),
    "password" : (value: "String", required: true, length(8, 64), availableChar)
]
```
<details>
  <summary><span style="color: Fuchsia; font-size: 16px"> Response </span></summary>
  <span style="color: Fuchsia"> redirect:/users/verify?id= <span> 
</details>


<br><span style="font-size: 18px !important; color: green"> RouteGroup(users/verify?id=)  </span></br>
<span style="color: red"> POST </span>
<br><span style="color: Fuchsia; font-size: 16px"> Request </span></br>

```json
[
    "code" : (value: "String", required: true, length(6), availableChar)
]
```
<span style="color: Fuchsia; font-size: 16px"> Response </span>

```json
Cookie:
accessToken
refreshToken
```


<br><span style="font-size: 18px !important; color: green"> RouteGroup(users/token) </span></br>
<span style="color: red"> POST </span>
<details>
  <summary><span style="color: Fuchsia; font-size: 16px"> Request </span></summary>
  <span style="color: Fuchsia"> Данный Route нужен для получения access токена <span> 
</details>

```json
Cookie:
refreshToken
```
<span style="color: Fuchsia; font-size: 16px"> Response </span>
```json
Cookie:
accessToken
```







<br><span style="font-size: 18px !important; color: green"> RouteGroup(users/refresh) </span></br>
<span style="color: red"> POST </span>
<br><span style="color: Fuchsia; font-size: 16px"> Request </span></br>

```json
Cookie:
refreshToken
```
<span style="color: Fuchsia; font-size: 16px"> Response </span>
```json
Cookie:
accessToken
refreshToken
```


<br><span style="font-size: 18px !important; color: green"> RouteGroup(users/logout) </span></br>
<span style="color: red"> POST </span>
<br><span style="color: Fuchsia; font-size: 16px"> Request </span></br>

```json
Cookie:
accessToken
```

<details>
  <summary><span style="color: Fuchsia; font-size: 16px"> Response </span></summary>
  <span style="color: Fuchsia"> StatusCode <span> 
</details>


<br><span style="font-size: 18px !important; color: green"> Route(users/confirmEmail) </span></br>
<span style="color: red"> POST </span>
<br><span style="color: Fuchsia; font-size: 16px"> Request </span></br>

```json
Cookie
accessToken
```

<details>
  <summary><span style="color: Fuchsia; font-size: 16px"> Response </span></summary>
  <span style="color: Fuchsia"> StatusCode <span> 
</details>

<br><span style="font-size: 18px !important; color: green"> Route(users/acceptEmail) </span></br>
<span style="color: red"> POST </span>
<br><span style="color: Fuchsia; font-size: 16px"> Request </span></br>

```json
[
    "code" : string
]
```

<details>
  <summary><span style="color: Fuchsia; font-size: 16px"> Response </span></summary>
  <span style="color: Fuchsia"> redirect:/main <span> 
</details>

<br></br>
<br></br>





Coming soon...