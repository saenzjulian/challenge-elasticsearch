# Search Tool

## Challenge

Develop a tool that allows searching for a keyword in a set of +11,800 text files.

## Requirements

- Coverage of unit tests.
- Versioned code.
- Documentation to execute the code tool.
- The keyword search doesn't have a logical order.
- When a keyword is composed, the search criteria must be `AND`.
- Process executed up to 1ms.
- The set of files can be pre-processed.

---

## Architecture

![Architecture](./diagram/bulksearch-arch.png)

## Project structure

```bash
```

## Initial considerations

The developed tool has a structure to be executed and the allowed commands are described below:

| **Option** | **First Argument** | **Second Argument** | **Example** | **Consider** |
|:---:|:---:|:---:|:---:|:---:|
| upload | index-name | path | upload my-index "./data" | the second argument must be contained in double quotes |
| search | index-name | keyword | search my-index "walt disney" | the index-name must be the same that was used to upload the files |
| native-search | index-name | keyword | native-search my-index "julian" | the index-name must be the same that was used to upload the files |

## How to prepare the environment?

### ***Step 01***

Run the `docker-compose.yml` file with the command:

```powershell
docker-compose up -d
```

We can now access ***ElasticSearch*** and ***Kibana*** instances at `http://localhost:9200` and `http://localhost:5601` respectively.

In case you don't have Docker installed, [use this tutorial.](https://linuxhint.com/install-docker-compose-windows/)

### ***Step 02***

Populate the ElasticSearch instance with information from the text files found in the `./data`.

This process can be executed through the tool with the command:

```bash
java -jar bulksearch-app.jar upload <index-name> "./data"
```

Don't forget to remove the **'<>'** characters and change to the correct index.

## How to run a search?

You have two options to perform a search:

### ***Option 01***

Run the next command to search with [Elasticsearch Java API Client](https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/current/index.html)

```bash
java -jar bulksearch-app.jar search <index-name> "walt disney"
```

### ***Option 02***

Run the next command to search with Rest native request

```bash
java -jar bulksearch-app.jar native-search <index-name> "walt disney"
```

The result for both queries will have the same result, the only difference is the response speed.

Result with `search` option

![Result Of Search](./diagram/result-search.png)

Result with `native-search` option

![Result Of Native-Search](./diagram/result-native-search.png)

---

# Technical section

## Stack

| Technology | Using |
|:---:|:---:|
| ![My Skills](https://skills.thijs.gg/icons?i=java&theme=light) | Build application |
| ![My Skills](https://skills.thijs.gg/icons?i=bash&theme=light) | Run commands |
| ![My Skills](https://skills.thijs.gg/icons?i=docker&theme=light) | Prepare environment |
| <img width = '50px' title="ElasticSearch" align= 'center' src="https://user-images.githubusercontent.com/9143253/47912437-f749bc00-de98-11e8-9669-e97f58b8be2e.png"> | Index data |

## Running unit tests

![Result Of Search](./diagram/execution-unit-tests.png)

Ejecuta v??a comando las pruebas unitarias con el siguiente comando:

```bash
> cd ./unit-test
> java -cp "junit-4.13.2.jar;." org.junit.runner.JUnitCore <ClassNameToTest>
```

## Query used to retrieve data

```json
get /index-name/_search
{
  "_source": ["field-to-search"],
  "query": {
    "match": {
      "content": {
        "query": "words keys",
        "operator": "and"
      }
    }
  },"sort": [
    {
      "title": {
        "order": "asc"
      }
    }
  ]
}
```

