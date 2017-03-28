# Developer notes

## Prerequisites
  * Java version >= 1.8
  * sbt version >= 0.13.13

## Build
### Locally

Make sure you have all the prerequisites installed.
To run locally, simply run 

```
sbt run
```

### CI

This project use [Travis CI](https://travis-ci.org/) as the CI build system. 
Visit [here](https://travis-ci.org/scw1109/servant) to see the build status.
 
#### Setup notes

Travis CI is sync with the personal Github repositories, 
which allow one to turn on/off each Github repo on Travis CI account management page.

## Release / Deploy

This project will automatically deploy to [Heroku](https://heroku.com)
  
### Setup notes

Followed the guide [here](https://devcenter.heroku.com/articles/github-integration#automatic-deploys) 
to setup Github integration and automatic deploy.

