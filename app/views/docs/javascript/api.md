2013년 2월 기준, 자바스크립트 API를 통해 할수 있는 것들은 아래와 같습니다.

0. 모델 데이터 넣기
0. 모델과 관계 연결하기
0. 관계 검색 결과 가져오기
0. 주문 결과 가져오기
0. 네트워크 장애 발생시 로컬에 백업 및 자동 복구

반면, 아래의 기능은 현재 불가능합니다.

0. OAuth 인증하기
0. Push 알림 받기 (Publish & Subscribe)

### 시작하기
#### 설치하기

`<head> ... </head>` 태그 안에 아래의 코드를 넣습니다.


<textarea class="code" style="width:95%; height:200px">
<script type="text/javascript">
(function(e){var t,n,r;n=e.createElement("script");n.type="text/javascript";n.async=!0;r="//192.168.0.5:9000/assets/javascripts/mintpresso-0.1.min.js";if("https:"===e.location.protocol){n.src="https:"+r}else{n.src="http:"+r}t=e.getElementsByTagName("script")[0];return t.parentNode.insertBefore(n,t)})(document);window.mintpresso.init("YOUR API KEY HERE")
</script>
</textarea>

위의 코드는 민트프레소 API 스크립트 파일을 브라우저에서 비동기<small>asynchronously</small>적으로 불러오게하여 사이트 전반적인 스크립트 로딩 속도에 영향을 주지 않도록 하였습니다.

이제 민트프레소 패널에서 해당 사이트로부터 데이터를 받기 시작합니다. 추가적으로, API 키 조회/변경 그리고 설정은 자신의 <a href="@routes.Panel.overview(user.id)">패널</a>에서 할 수 있습니다.

<blockquote>
  기본적으로 <code>window.mintpresso</code>에 API가 초기화되므로 <code>mintpresso.set</code> 이와 같이 이용할 수 있습니다. 만약 scope가 겹치거나 여러가지 이유로 API Object 이름을 바꾸는 경우 <a href="@routes.Panel.overview(user.id)#!/api" target="_blank">API 설정</a>에서 가능합니다.
</blockquote>

#### 데이터가 잘 들어오는지 확인하기

이미지

자신의 <a href="@routes.Panel.overview(user.id)" target="_blank">패널</a>에서 설치여부를 바로 확인할 수 있습니다. 메뉴의 'syncing ...' 아이콘이 회전하고 있고, OVERVIEW에서 최근에 데이터를 받은 시간이 표시됩니다.

#### 모델 데이터를 넣기 

>
mintpresso.set( jsonObject[, updateIfExists = true] )

>
{ 
  <strong>String</strong> type: <strong>String</strong> identifier *[,
    <strong>String</strong> key: <strong>String</strong> value
  ]
}

민트프레소 <a href="" target="_blank">모델 정의</a>에 따라 type, identifier, data를 지정해야합니다.

이메일 주소가 @user.email인 사용자를 추가하려면

>```
mintpresso.set({
  "type": "user",
  "identifier": "@user.email"
})
/* key가 모델의 종류, value가 식별자. 회원은 unique 해야함 */
```

또는 이와 같이 쓸 수도 있습니다.
>```
mintpresso.set({
  "user": "@user.email"
})
/* String인 경우 identifer로, Number인 경우 모델의 아이디로 간주함 */
```

이메일 주소가 @user.email인 회원의 최근 로그인 시간을 업데이트 하려면

>```
mintpresso.set({
  "user": "@user.email",
  "logged_at": Date.now
})
/* 모델 타입과 동사 지시어인 "user", "page", do", "does", "did" 등을 제외한 모든 문자열이 key가 될 수 있음 */
```

내부 데이터베이스 상 회원 아이디가 1001인 사용자의 첫 방문 페이지를 추가하려면

>```
mintpresso.set({
  "user": "1001",
  "landing_page": window.location.href
}, false) /* 이미 landing_page 값이 있으면 업데이트 안함 */
```

위와 같은 의도로, 1001 아이디를 가진 사용자에게 최초 결제일을 추가하려면

>```
mintpresso.set_once({
  "user": "10020",
  "initial_purchase_at": Date.now()
}) /* set의 두번째 parameter에 false를 준 것과 똑같이 작동함 */
```

#### 모델과 관계를 만들고 가져오기

<pre>
mintpresso.get({
  <strong>String</strong> subject type: (<strong>String</strong> identifier | <strong>Number</strong> modelNo | "?"),
  <strong>String</strong> (verb | do | does | did): (<strong>String</strong> verb | "?"),
  <strong>String</strong> object type: (<strong>String</strong> identifier | <strong>Number</strong> modelNo | "?"),
})
</pre>

사용자별 상품페이지 이동을 tracking하기 위해 아래와 같이 현재 페이지의 정보를 매번 추가할 수 있습니다. 사용자는 같은 페이지를 여러번 방문할 수 있고 중복 또한 저장해야하므로 identifier를 지정하지 않습니다. (non-unique)

>```
mintpresso.set({
  "type": "page",
  "url": window.location.href,
  "user_id": user.id
})
```

하지만 만약 @user.email 이메일주소를 가진 회원이 최근에 본 상품들을 가져오려면 어떻게 해야할까요? 민트프레소에서는 조건문을 입력하는 복잡한 방법을 쓰지 않습니다.

>```
mintpresso.get({
  "user": "@user.email",
  "do": "view",
  "page": "?"
})
```

즉, "Who<small>(user)</small> reads what<small>(page)</small>?"를 질문하는 것과 같습니다. 질문에서 user에게 힌트를 주었으므로 모르고 있는 page를 쉽고 빠르게 가져올 수 있습니다. 이와 비슷하게 "who follows me?", "who am I following?"를 매우 쉽게 처리할 수 있습니다.

동사는 자유롭게 지정가능하며 가급적 단수-현재형 동사<small>(singular present verb)</small>를 권장합니다. 대표적으로 'read', 'like', 'follow', 'view', 'star'가 있습니다. 반드시 'do', 'does', 'did', 'verb'를 `key`로 하여 있을 때만 검색으로 처리됩니다.

<pre><code>
/* example */

function getPreviousProducts(callback) {
  var result = mintpresso.get({
    user: user.email, do: "view", "product": "?"
  }, {limit: 3});
  callback(result);
}
</code></pre>

위의 예제와 같이 limit와 offset을 지정 가능합니다.

* `get({ ... }, { “limit”: 20, “offset”: 40})`
* `getLimit({ ... }, 20)`
* `getLimitOffset({ ... }, 20, 40)`