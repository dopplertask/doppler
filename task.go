package main

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
)

func main() {
	MakeRequest()
}

type Task struct {
	Id      int
	Name    string
	Created string
}

func MakeRequest() {
	resp, err := http.Get("http://localhost:8090/task")
	if err != nil {
		log.Fatalln(err)
	}

	body, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		log.Fatalln(err)
	}

	var myStoredVariable []Task

	jsonErr := json.Unmarshal([]byte(body), &myStoredVariable)

	if jsonErr != nil {
		log.Fatalln(jsonErr)
	}

	fmt.Printf("List prime")
	fmt.Printf("Birds : %+v", myStoredVariable[0].Name)

}
