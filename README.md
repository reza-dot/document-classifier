# document-classifier

web service that assigns specific PDF documents to predefined classes based on their language
and layout.

## Description

Hybrid approach of a machine learning and rule-based system for training and classifying structured PDF documents.
A complete description of the implementation can be found in the bachelor thesis.

## Getting Started

### Dependencies

* Java 17
* Apache Maven 3.8.5 (If you want to build from source)
* Docker (if you want to start the app inside a docker container)

### Build from source
Execute the following command inside the project folder
```
mvn package
```
### Executing program
```
java -jar document-classifier-<VERSION>.jar
```
### Run in docker
Execute the following command inside the project folder to run the web service inside a docker container
```
docker compose up
```

## Authors

Contributors names and contact info
[@RezaKhorrami](https://www.linkedin.com/in/reza-khorrami/)

## Version History

* 1.0
    * Initial Release

## License

This project is licensed under the [APACHE LICENSE, VERSION 2.0](LICENSE)

## Acknowledgments

* [Apache PDFBox](https://github.com/apache/pdfbox)
* [Tess4j](https://github.com/nguyenq/tess4j)
