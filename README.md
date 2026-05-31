# spring-login-app

Spring Boot 4.x を使ったログイン機能付きWebアプリケーションです。
ThymeleafによるサーバーサイドレンダリングとREST APIの両方に対応しており、実務・本番向けの構成を意識して作成しました。

---

## 技術スタック

| カテゴリ | 技術 |
|---|---|
| 言語 | Java 21 |
| フレームワーク | Spring Boot 4.0.6 |
| セキュリティ | Spring Security 7.x |
| テンプレートエンジン | Thymeleaf |
| ORM | Spring Data JPA / Hibernate |
| DBマイグレーション | Flyway |
| 開発用DB | H2 Database |
| 本番用DB | PostgreSQL |
| ビルドツール | Maven |
| テスト | JUnit5 / Mockito / Testcontainers |
| 監視 | Spring Boot Actuator |

---

## 機能一覧

- ユーザー登録（バリデーション・重複チェック）
- ログイン / ログアウト
- ログイン後のマイページ表示
- REST API（ユーザー情報取得・登録・アカウント削除）
- Actuatorによる死活監視（`/actuator/health`）

---

## アーキテクチャ

    src/main/java/com/example/demo/
    ├── config/
    │   ├── SecurityConfig.java               # 認証・認可設定
    │   └── ActuatorSecurityConfig.java       # Actuatorエンドポイント保護
    ├── controller/
    │   ├── web/                              # Thymeleaf画面用
    │   │   ├── LoginController.java
    │   │   ├── RegisterController.java
    │   │   └── UserController.java
    │   └── api/v1/                           # REST API用
    │       └── UserApiController.java
    ├── dto/
    │   ├── request/
    │   │   └── RegisterRequest.java
    │   └── response/
    │       ├── UserResponse.java
    │       └── ErrorResponse.java
    ├── entity/
    │   └── User.java
    ├── repository/
    │   └── UserRepository.java
    ├── service/
    │   ├── UserService.java
    │   └── UserDetailsServiceImpl.java
    ├── exception/
    │   ├── UserNotFoundException.java
    │   ├── DuplicateEmailException.java
    │   └── GlobalExceptionHandler.java
    └── validation/
        ├── PasswordConfirm.java
        └── PasswordConfirmValidator.java

---

## 設計上のポイント

**画面系とAPI系の共存**
`/` 配下はThymeleafによるサーバーサイドレンダリング、`/api/v1/` 配下はREST APIとして共存しています。SecurityConfigで未認証時の振る舞いを振り分けており、画面系はログインページへリダイレクト、API系は401を返します。

**CSRF対策**
画面系はCookieベースのCSRFトークンを有効にし、REST APIは無効にしています。これによりブラウザからのフォーム送信は保護しつつ、APIクライアントからのリクエストは柔軟に受け付けられます。

**Flywayによるスキーマ管理**
`spring.jpa.hibernate.ddl-auto=validate` に設定し、スキーマ変更はすべてFlywayのマイグレーションファイルで管理しています。本番環境でも安全にスキーマ変更を適用できます。

**エラーハンドリングの一元化**
`GlobalExceptionHandler`（`@RestControllerAdvice`）で例外を一元処理し、API系はJSON、画面系はエラーページを返します。同じ例外でもリクエストの種別によってレスポンス形式を切り替えています。

**セキュリティヘッダーの強化**
HSTS・X-Frame-Options・Content-Type-Options・Referrer-Policyを明示的に設定し、クリックジャッキングやMIMEスニッフィングなどの攻撃を防いでいます。

**ユーザー列挙攻撃への対策**
ログイン失敗時に「メールアドレスが存在しない」「パスワードが違う」を区別せず「認証に失敗しました」と統一したメッセージを返すことで、攻撃者が登録済みメールアドレスを特定できないようにしています。

**UserServiceのインターフェースを設けない理由**
実装が1つのみで差し替えの予定がないため、過剰な抽象化を避けシンプルさを優先しました。インターフェースが必要になった時点で導入する方針です。

---

## 開発で直面した課題と解決策

**Spring Boot 4.x / Spring Security 7.x の互換性問題**
`AntPathRequestMatcher` が削除され `PathPatternRequestMatcher` への移行が必要でした。また `defaultAuthenticationEntryPoint`（1引数版）が廃止されており、`AnyRequestMatcher.INSTANCE` を使った `defaultAuthenticationEntryPointFor` への置き換えで解決しました。ログイン画面への無限リダイレクトが発生しないよう、`authorizeHttpRequests` の `permitAll()` との連携も慎重に設計しました。

**テストでのSpring Security統合問題**
`@WebMvcTest` では `ActuatorSecurityConfig` が読み込まれてEndpointRequest関連のBeanが存在せず500エラーが発生しました。また `@SpringBootTest` + `@AutoConfigureMockMvc` では `@WithMockUser` が効かず401が返る問題が起きました。最終的に `MockMvcBuilders.webAppContextSetup(context).apply(springSecurity())` で明示的にSpring Securityを統合することで解決しました。

**Testcontainersのバージョン互換性問題**
Docker 29.x とTestcontainers 1.20.x の組み合わせで `Could not find a valid Docker environment` エラーが発生しました。Testcontainersを1.21.4以上にアップデートすることで解決しました。

---

## テスト構成

| 種別 | クラス | 観点 |
|---|---|---|
| ユニットテスト | `UserRepositoryTest` | H2を使ったRepository層の動作確認 |
| ユニットテスト | `UserServiceTest` | Mockitoを使ったビジネスロジックの確認 |
| ユニットテスト | `RegisterControllerTest` | 登録フォームのバリデーション・画面遷移 |
| ユニットテスト | `UserApiControllerTest` | APIエンドポイントの認証・レスポンス確認 |
| 結合テスト | `UserRepositoryIntegrationTest` | PostgreSQL実機でのユニーク制約・型確認 |
| 結合テスト | `RegisterControllerIntegrationTest` | 登録→ログインの一連フロー確認 |
| 結合テスト | `UserApiControllerIntegrationTest` | 登録→ログイン→取得→削除→再ログイン失敗の一連フロー確認 |

---

## APIエンドポイント一覧

| メソッド | パス | 認証 | 説明 |
|---|---|---|---|
| GET | `/login` | 不要 | ログイン画面 |
| POST | `/login` | 不要 | ログイン処理（Spring Security） |
| GET | `/register` | 不要 | 登録画面 |
| POST | `/register` | 不要 | ユーザー登録（画面） |
| GET | `/user` | 必要 | マイページ |
| POST | `/logout` | 必要 | ログアウト |
| GET | `/api/v1/user/me` | 必要 | ログイン中ユーザー情報取得 |
| POST | `/api/v1/user/register` | 不要 | ユーザー登録（API） |
| DELETE | `/api/v1/user/me` | 必要 | アカウント無効化 |
| GET | `/actuator/health` | 不要 | ヘルスチェック |
| GET | `/actuator/**` | Basic認証 | Actuatorその他 |

---

## 起動手順

### 前提条件

- Java 21
- Maven 3.9+
- Docker（結合テスト実行時のみ必要）

### 開発環境での起動

```bash
# リポジトリのクローン
git clone https://github.com/SK-Shun/spring-login-app.git
cd spring-login-app

# 起動（devプロファイル・H2インメモリDB）
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

ブラウザで `http://localhost:8080/register` にアクセスしてユーザー登録から始めてください。

### テストの実行

```bash
# ユニットテスト
mvn test -Dspring.profiles.active=test

# 結合テスト（Docker起動が必要）
mvn test -Dspring.profiles.active=integration
```

---

## 開発状況

- [x] フェーズ1：土台（DBとエンティティ）
- [x] フェーズ2：認証基盤
- [x] フェーズ3：登録機能
- [x] フェーズ4：ログイン画面（Thymeleaf）
- [x] フェーズ5：例外ハンドリング
- [x] フェーズ6：REST API
- [x] フェーズ7：テストコード（ユニット・結合）
