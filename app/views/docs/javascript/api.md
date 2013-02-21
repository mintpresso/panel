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


<textarea class="code" style="width:95%; height:250px">
<script type="text/javascript">
(function(e,t){var n,r,i;r=e.createElement("script");r.type="text/javascript";r.async=!0;r.onload=function(){return window["mintpresso"].init(t)};i="//192.168.0.5:9000/assets/javascripts/mintpresso-0.1.min.js";if("https:"===e.location.protocol){r.src="https:"+i}else{r.src="http:"+i}n=e.getElementsByTagName("script")[0];return n.parentNode.insertBefore(r,n)})(document,"YOUR API KEY HERE")
</script>
</textarea>

위의 코드는 민트프레소 API 스크립트 파일을 브라우저에서 비동기<small>asynchronously</small>적으로 불러오게하여 사이트 전반적인 스크립트 로딩 속도에 영향을 주지 않도록 하였습니다.

이제 민트프레소 패널에서 해당 사이트로부터 데이터를 받기 시작합니다. 추가적으로, API 키 조회/변경 그리고 설정은 자신의 <a href="@routes.Panel.overview(user.id)">패널</a>에서 할 수 있습니다.

<div class="alert alert-block alert-info">
  기본적으로 <code>window.mintpresso</code>에 API가 초기화되므로 <code>mintpresso.set</code> 이와 같이 이용할 수 있습니다.
</div>

#### 데이터가 잘 들어오는지 확인하기

이미지

자신의 <a href="@routes.Panel.overview(user.id)" target="_blank">패널</a>에서 설치여부를 바로 확인할 수 있습니다. 메뉴의 'syncing ...' 아이콘이 회전하고 있고, OVERVIEW에서 최근에 데이터를 받은 시간이 표시됩니다.

#### 모델 데이터를 넣기 

>
mintpresso.set( **Object** json[, **Boolean** updateIfExists = true] )

민트프레소 <a href="" target="_blank">모델 정의</a>에 따라 type, identifier, data를 지정해야합니다.

첫번째 인자인 json의 key가 모델의 종류, value가 식별자가 됩니다. 회원 모델의 경우 unique 해야하므로 일반적으로 이메일 주소를 identifier로 이용합니다. 이메일 주소가 @user.email인 사용자를 추가하려면

<pre><code>
mintpresso.set({
  "type": "user",
  "identifier": "@user.email"
})
</code></pre>

또는 이와 같이 쓸 수도 있습니다.

<pre><code>
mintpresso.set({
  "user": "@user.email"
})
</code></pre>

<div class="alert alert-block alert-warn">
  value가 String인 경우 identifer로, Number인 경우 모델의 아이디로 간주함
</div>

기본 모델 설명과 더불어 추가적인 데이터를 넣을 수 있습니다. 이메일 주소가 @user.email인 회원의 최근 로그인 시간을 업데이트 하려면 아래와 같이 새로운 key로 정의합니다.

<pre><code>
mintpresso.set({
  "user": "@user.email",
  "logged_at": Date.now
})
</code></pre>

<div class="alert alert-block alert-warn">
모델 타입과 동사 지시어인 "user", "page", do", "does", "did" 문자열은 data의  key가 될 수 없음
</div>

내부 데이터베이스 상 회원 아이디가 1001인 사용자의 첫 방문 페이지를 추가하려면 최초 첫번째 변경만 허용하고 다음의 수정은 막아야합니다. 이때는 `updateIfExists` 인자를 이용해 2차적인 수정을 막을 수 있습니다.

<pre><code>
mintpresso.set({
  "user": "1001",
  "landing_page": window.location.href
}, false) /* 이미 landing_page 값이 있으면 업데이트 안함 */
</code></pre>

위와 같은 의도로, `set_once`를 이용할수 있습니다. 1001 아이디를 가진 사용자에게 최초 결제일을 추가하려면

<pre><code>
mintpresso.set_once({
  "user": "10020",
  "initial_purchase_at": Date.now()
}) /* set의 updateIfExists parameter에 false를 준 것과 똑같이 작동함 */
</code></pre>

#### 모델과 관계를 만들고 가져오기

>
mintpresso.get( **Object** json, **Function** callback[, **Object** optionsObject] )

<!--
<pre>jsonObject =
{
  <strong>String</strong> subjectType: (<strong>String</strong> identifier | <strong>Number</strong> modelNo | "?"),
  <strong>String</strong> (verb | do | does | did): (<strong>String</strong> verb | "?"),
  <strong>String</strong> objectType: (<strong>String</strong> identifier | <strong>Number</strong> modelNo | "?"),
})
</pre>

<pre>callbackFunction = 
function (result: Object) { ... }
</pre>

<pre>optionsObject = 
{
  limit: Number,
  offset: Number
}
</pre>
-->

사용자별 상품페이지 이동을 tracking하기 위해 아래와 같이 현재 페이지의 정보를 매번 추가할 수 있습니다. 사용자는 같은 페이지를 여러번 방문할 수 있고 중복 또한 저장해야하므로 identifier를 지정하지 않습니다. (non-unique)

<pre><code>
mintpresso.set({
  "type": "page",
  "url": window.location.href,
  "user_id": user.id
})
</code></pre>

하지만 만약 @user.email 이메일주소를 가진 회원이 최근에 본 상품들을 가져오려면 어떻게 해야할까요? 민트프레소에서는 조건문을 입력하는 복잡한 방법을 쓰지 않습니다.

<pre><code>
mintpresso.get({
  "user": "@user.email",
  "do": "view",
  "page": "?"
}, callback)
</code></pre>

즉, "Who<small>(user)</small> reads what<small>(page)</small>?"를 질문하는 것과 같습니다. 질문에서 user에게 힌트를 주었으므로 모르고 있는 page를 쉽고 빠르게 가져올 수 있습니다. 이와 비슷하게 "who follows me?", "who am I following?"를 매우 쉽게 처리할 수 있습니다.

<div class="alert alert-block alert-info">동사는 자유롭게 지정가능하며 가급적 단수-현재형 동사<small>(singular present verb)</small>를 권장합니다. 대표적으로 'read', 'like', 'follow', 'view', 'star'가 있습니다. 반드시 'do', 'does', 'did', 'verb'를 `key`로 하여 있을 때만 검색으로 처리됩니다.</div>

이제, 가져온 데이터를 받아오려면 callback을 잘 이용해야합니다.

<pre><code>
var phone_number = "010-0000-0000"
mintpresso.get(phone_number, function (result) {
  if (result.status.code == 200) {
    alert(  ["안녕하세요 ",
            result.point.username,
            "님! "].join() )
  } else {
    alert( "처음 들어오셨군요! 이름을 입력해주세요." )
  }
}
</code></pre>

<pre><code>
function getPreviousProducts(callback) {
  return mintpresso.get({
    user: user.email, do: "view", "product": "?"
  }, callback, {limit: 3});
}

$('button#seeMore').click( function(){
  $list = $(this).closest('.list')
  getPreviousProducts( function(products){
    for(item in products){
      $list.append( item.name )
    }
  })
});
</code></pre>
위의 예제와 같이 `options` 필드에 limit와 offset와 같은 여러개의 옵션을 지정할 수 있습니다.

<div class="alert alert-block alert-info">
Option의 추가/변경에 따른 코드 변화를 최소화하기 위해 가급적 json object를 이용하는 것을 권장합니다. 아래의 추가 함수를 통해 직접 명시<small>explicit</small>할 수도 있습니다.
<code>get({ ... }, callback, { “limit”: 20, “offset”: 40})</code><br />
<code>getLimit({ ... }, callback, 20)</code><br />
<code>getLimitOffset({ ... }, callback, 20, 40)</code>
</div>