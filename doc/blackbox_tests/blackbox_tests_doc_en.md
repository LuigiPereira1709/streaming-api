# Blackbox Test Doc
[Versão em Português](blackbox_tests_doc_pt.md)  
Blackbox testing the endpoint of the API is a crucial step to ensure that the API behaves as expected from a user's perspective. They are executed manually or automatically, with exceptions being made for certain conditions or timeout scenarios. The tests are designed to verify the functionality of the API without delving into its internal workings.

## Test Cases
The test cases are structured to cover various aspects of the API, including:
- **Post Requests**: 
    1. Testing the creation of resources, ensuring that the API correctly handles the input data and returns the expected response.
    2. Validating that the API responds appropriately to invalid input, such as missing required fields or incorrect data types.

- **Put Requests**:
    1. Testing the update of existing resources, ensuring that the API correctly modifies the data and returns the expected response.
    2. Validating that the API handles invalid updates gracefully, such as attempting to update a resource that does not exist or providing invalid data.

- **Search Requests**:
    1. Testing the retrieval of resources based on specific criteria, ensuring that the API returns the correct data.
    2. Validating that the API handles invalid search queries appropriately, such a as searching for resources that do not exist or providing malformed search parameters. 
- **Delete Requests**:
    1. Testing the deletion of existing resources, ensuring that the API correctly removes the specified resource and returns the expected response.
    2. Validating that the API handles attempts to delete non-existent resources gracefully, returning an appropriate error message.
- **Get Content URL**:
    1. Testing the generation of content URLs, ensuring that the API returns valid valid URLs for consuming content.
    2. Validating that the API handles requests for content URLs that do not exist or are malformed, returning appropriate error messages.

## Expected Outcomes
The expected outcomes of the tests include:
- [x] Successful creation, update, retrieval, and deletion of resources.
- [x] Correct handling of invalid input, including appropriate error messages and status codes.
- [x] Proper generation of content URLs, ensuring they are valid and accessible.
- [x] Consistent and predictable behavior of the API across different test cases.

## Execution
The tests are executed using [hurl](https://hurl.dev/), tool based on curl for testing requests and responses. To run the tests, an api for test should be set up and the following command can be used:
### Pre-requisites
- **[Hurl 6.1.1](https://hurl.dev/docs/installation.html)**: A command-line tool for testing HTTP requests and responses.
- **MongoDB**: A `NoSQL` database to store the data used in the tests.

### Step by Step
1. Navigate to the directory containing the test files.
2. Load the script `insertOne.js` into the database to ensure the database is populated with the necessary data for testing. Use the following command:
```bash
mongosh <your_mongo_uri> --eval "load('**/insertOne**.js')"
```
3. Run the hurl tests using the following command, replacing `name_of_test.hurl` with the specific test file you want to execute:
```bash
hurl --file-root .. --verbose name_of_test.hurl 
```

## Structure
``` plaintext
blackbox_test/
│
├── music/
│   ├── post.hurl
│   ├── put.hurl
│   ├── search.hurl
│   ├── delete.hurl
│   ├── get_content_url.hurl
│   └── insertOneMusic.js
│
├── video/
│   ├── post.hurl
│   ├── put.hurl
│   ├── search.hurl
│   ├── delete.hurl
│   ├── get_content_url.hurl
│   └── insertOnePodcast.js
│
├── content.mp3
├── thumbnail.jpeg
└── txt.txt
```

## Future Improvements
- [ ] **Improve the test sequence**: add timeouts between requests.
- [ ] **Use environment variables**: Instead of hardcoding values in the test files, use environment variables to make the tests more flexible and adaptable to different environments.
- [ ] **Add more test cases**: Cover additional scenarios, such as edge cases or specific error conditions that may not be currently addressed.
- [ ] **Add more error handling**: Ensure that the API gracefully handles unexpected conditions, such as network failures or server errors.
- [ ] **Add stress tests**: Implement stress tests to evaluate the API's performance under heavy load, ensuring it can handle a large number of requests without degrading performance.

## Links
[Hurl doc](https://hurl.dev/)  
[To the blackbox tests](../../blackbox_tests/)  
[Back to the main doc](../../README.md)  

## Learning Goals?
- [ ] **Gain a deeper understanting about hurl**: Learn how to effectively use hurl for testing APIs, including its features and capabilities.
- [ ] **Gain experience in writing black box tests**: Develop skills in creating comprehensive test cases that cover various scenarios and edge cases.
- [ ] **Learn about API testing best practices**: Understand the principles of effective API testing, including how to structure tests, handle errors, and validate responses.
- [ ] **Learn about stress testing**: Understand the importance of stress testing in API development and how to implement it effectively.
