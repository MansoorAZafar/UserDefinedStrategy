# User Defined Strategy 

A console application that let's users either use pre-defined payment strategies or define their own custom payment strategy to be used. 
When choosing pre-defined users are allowed to choose which constructor to use and pass in any value(s). 
By default, it comes with a basic Credit Card and Gift Card strategy.

**Live Demo:**  

## Demo
<p align="center"> 
  <img src="https://github.com/user-attachments/assets/4e331589-37a0-42d7-b125-a52297bd8682" width="900" alt="Overview" /> 
</p> 
<table> 
  <tr> 
    <td width="50%"> 
      <img src="https://github.com/user-attachments/assets/3740037b-3f31-46e2-988a-d1312ba3fbce" width="100%" alt="Main interface" /> 
    </td> 
    <td width="50%"> 
      <img src="https://github.com/user-attachments/assets/c643080a-e409-43af-b85c-8fdeda15d629" width="100%" alt="Secondary interface" /> 
    </td> 
  </tr> 
  <tr> 
    <td width="50%"> <img src="https://github.com/user-attachments/assets/91711d9c-1ccf-46ad-8014-ba366d64fb57" width="100%" alt="Feature view" /> 
    </td> <td width="50%"> 
      <img src="https://github.com/user-attachments/assets/766a3fa8-0e6e-47e3-99db-79a4724af909" width="100%" alt="Additional view" /> 
    </td> 
  </tr> 
</table>

**Video demo:**

https://github.com/user-attachments/assets/d89bf448-8a82-4298-b480-be5c88df8d23




## Getting Started
**Prerequisites**
- Java 21 (pom.xml compiles with java 21 but can use a lower version)

**Development**
```bash

mvn clean compile exec:java
```

**Executable Jar**
```bash
mvn clean package
java -jar .\target\uds-1.0-SNAPSHOT.jar
```
