# document-classifier

web service that assigns specific PDF documents to predefined classes based on their language
and layout.

## Description

Hybrid approach of a machine learning and rule-based system for training and classifying structured PDF documents.

## Getting Started

### Dependencies

* JDK 17
* Apache Maven 3.8.5 (If you want to build from source)
* Docker (if you want to start the app inside a docker container)

### Build from source
Execute the following command in the project folder
```
mvn package
```
### Run in docker
If you want to start the webservice in the docker container execute the following command in the solution
```
docker compose up
```
### Executing program (local)
```
java -jar document-classifier-<VERSION>.jar
```

## Authors

Contributors names and contact info
[@RezaKhorrami](https://www.linkedin.com/in/reza-khorrami/)

## Version History

* 0.1
    * Initial Release

## License

This project is licensed under the [APACHE LICENSE, VERSION 2.0](https://www.apache.org/licenses/LICENSE-2.0) License - see the LICENSE.md file for details

## Acknowledgments

* [Apache PDFBox](https://github.com/apache/pdfbox)
* [Tess4j](https://github.com/nguyenq/tess4j)
