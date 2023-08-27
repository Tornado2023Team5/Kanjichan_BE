FROM gradle:latest AS builder

# 作業ディレクトリの設定
WORKDIR /opt/app

# ソースコードをコンテナにコピー
COPY ./JavaBE /opt/app

# Gradleを使用してビルドを実行
RUN gradle bootJar

# 実行ステージ
FROM openjdk:20

# 環境変数の設定
# (NOTE: .env ファイルの内容を直接 Dockerfile にコピペする必要があります)
# 例:
# ENV MY_ENV_VAR=value

# ビルドされたJARファイルをビルダーコンテナからコピー
COPY --from=builder /opt/app/build/libs/Kanjichan-0.0.1.jar /opt/Kanjichan.jar

# コンテナ起動時のコマンドを指定
CMD ["java", "-jar", "/opt/Kanjichan.jar"]