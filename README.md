This is an example of an application deployed in a liferay portal docker container.



The `liferay/files` dir content is copied to `/opt/liferay` in the container. Go see the [liferay portal page on docker hub](https://hub.docker.com/r/liferay/portal) to explore further configurations.

## Launch

```sh
docker compose up -d --build
```

Before launching, you should create the dockerfile with:
```sh
blade gw createDockerfile
```

You can deploy the modules with
```sh
blade gw deploy
```
and then manually drop the modules jars in `liferay/deploy` to hot deploy modules at runtime.