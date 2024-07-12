FROM python:3.9.12-alpine3.15

EXPOSE 5000

WORKDIR /app

COPY requirements.txt .

RUN pip install -r requirements.txt && \
    apk upgrade

COPY wsgi.py config.py ./
COPY application/ application/

CMD [ "python", "wsgi.py" ]