# ベースとなるイメージを指定
FROM gradle:latest

# 環境変数の設定
# (NOTE: .env ファイルの内容を直接 Dockerfile にコピペする必要があります)
# 例:
# ENV MY_ENV_VAR=value

# 作業ディレクトリの設定
WORKDIR /opt

# 必要なファイルやディレクトリをコンテナにコピー
COPY ./JavaBE/build/libs/Kanjichan-0.0.1.jar /opt/Kanjichan.jar

# コンテナ起動時のコマンドを指定
CMD ["java", "-jar", "Kanjichan.jar"]
