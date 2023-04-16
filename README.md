# be-java-cafe

마스터즈 2023 스프링 카페

## 배포 url

[Spring-cafe](http://ec2-3-34-194-155.ap-northeast-2.compute.amazonaws.com:8080/)

## 로그인

스프링에서 `HttpSession`을 이용해 로그인을 구현

* [POST "login"] `userId`, `password`를 받고 로그인을 처리합니다.
* 로그인 로직을 수행합니다.
* 로그인 성공 시 `SESSIONID` 를 발급한다.
* 로그인 실패 시 다시 `user/login` 뷰를 반환한다.

## 자신의 개인정보만 수정

* 로그인한 사용자는 자신의 정보를 수정할 수 있다.
* 쿠키로 넘어온 세션을 통해 개인정보를 수정할 수 있는 권한이 있는지 확인한다.
* 권한이 일치하면 개인정보 수정화면을 띄워주고 그렇지 않으면, 예외 화면을 띄워준다.

## 게시글 권한 부여

* 로그인하지 않은 사용자는 게시글의 목록만 볼 수 있다.
* 로그인한 사용자만 게시글의 세부내용을 볼 수 있다.
    * 세션 값이 존재하지 않으면 로그인 페이지로 이동시킨다.
* 로그인한 사용자만 게시글을 작성할 수 있다.
    * 글작성 화면에서 글쓴이 입력 필드를 삭제한다.
    * 로그인하지 않은 사용자가 글쓰기 페이지에 접근할 경우 로그인 페이지로 이동한다.
* 로그인한 사용자는 자신의 글을 수정할 수 있다.
    * 수정하기 폼과 수정하기 기능은 로그인 사용자와 글쓴이의 사용자 아이디가 같은 경우에만 가능하다.
    * 상황에 따라 "다른 사람의 글을 수정할 수 없다."와 같은 에러 메시지를 출력하는 페이지로 이동하도록 구현한다.
* 로그인한 사용자는 자신의 글을 삭제할 수 있다.
    * `@DeleteMapping`을 사용한다.
    * 삭제하기는 로그인 사용자와 글쓴이의 사용자 아이디가 같은 경우에만 가능하다.
    * 상황에 따라 "다른 사람의 글을 수정할 수 없다."와 같은 에러 메시지를 출력하는 페이지로 이동하도록 구현한다.
