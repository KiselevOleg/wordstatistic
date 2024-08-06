"use client";

import React from "react";
import Link from "next/link";
import Styles from "./page.module.css"

import { validTokens } from "@/api/userAPI";
import { Topic, getAllTotics, addTopic, updateTopic, deleteTopic } from "@/api/localstatisticAPI";

let reloadListNeeded:boolean=false;

export default class Page extends React.Component<unknown , unknown> {
  constructor(props: unknown) {
    super(props);

    this.state= {

    };
  }

  componentDidMount(): void {
    if(!validTokens()) window.location.replace("/auth/signIn");
  }

  render():React.ReactNode {
    return <>
      <TopicList />
      <AddNewTopicsForm />
    </>;
  }
}

interface StateTopicListType {
  topics:Topic[],
  selectedToChangeTopics: Map<string,{newName:string, errorMessage:string}>,
}
class TopicList extends React.Component<unknown,StateTopicListType> {
  constructor(props: unknown) {
    super(props);

    this.state={
      topics:[],
      selectedToChangeTopics:new Map<string,{newName:string, errorMessage:string}>()
    };
  }

  componentDidMount(): void {
    getAllTotics().then(res => {
      this.setState({topics: res});
    });
    setInterval(()=>{
      if(!reloadListNeeded) return;

      reloadListNeeded=false;
      getAllTotics().then(res => {
        this.setState({topics: res});
      });
    }, 100);
  }

  topicChangeButtonClickHandle = (topicName:string):void => {
    const {selectedToChangeTopics}=this.state;
    if (selectedToChangeTopics.has(topicName)) {
      selectedToChangeTopics.delete(topicName);
    } else {
      selectedToChangeTopics.set(topicName, {newName: "", errorMessage: ""});
    }
    this.setState({selectedToChangeTopics: selectedToChangeTopics});
  }
  topicChangeNewNameInputOnChangeHandle = (topicName:string, newValue:string):void => {
    const {selectedToChangeTopics}=this.state;
    const newTopic:{newName: string,errorMessage: string} = selectedToChangeTopics.get(topicName)!;
    selectedToChangeTopics.set(topicName, {newName: newValue, errorMessage: newTopic.errorMessage});
    this.setState({selectedToChangeTopics: selectedToChangeTopics});
  }
  topicChangeConfirmButtonOnClickHandle = (topicName:string):void => {
    const {selectedToChangeTopics}=this.state;
    const newTopic:{newName: string,errorMessage: string} = selectedToChangeTopics.get(topicName)!;
    if(!newTopic.newName.match("^[A-Za-z0-9_]{1,50}$")) {
      selectedToChangeTopics.set(
        topicName, 
        {
          newName: newTopic.newName, 
          errorMessage: "a new name must consist of A-Za-z0-9_ and have length from 1 to 50"
        }
      );
      this.setState({selectedToChangeTopics: selectedToChangeTopics});
      return;
    }
    selectedToChangeTopics.delete(topicName);
    updateTopic(topicName, newTopic.newName).then(res => {
      if(res===false) {
        selectedToChangeTopics.set(
          topicName, 
          {
            newName: newTopic.newName, 
            errorMessage: "a new name is already used"
          }
        );
        this.setState({selectedToChangeTopics: selectedToChangeTopics});
        return;
      }
      reloadListNeeded=true;
      this.setState({selectedToChangeTopics: selectedToChangeTopics});
    });
    
  }
  topicDeleteButtonClickHandle = (topicName:string):void => {
    deleteTopic(topicName).then(res => {
      if(res===false) return;

      reloadListNeeded=true;
    });
  }

  render():React.ReactNode {
    const {topics, selectedToChangeTopics}=this.state;
    const topics_:React.ReactNode[]=[];
    for(let i:number=0;i<topics.length;++i) {
      topics_[i]=<li key={topics[i].name}>
        <Link href="/statistic/data/topic/topic1/texts"><span>{topics[i].name}</span></Link>
        <button onClick={() => this.topicChangeButtonClickHandle(topics[i].name)} 
          className={Styles.manipulationButton} title="change">=</button>
        {
          selectedToChangeTopics.has(topics[i].name)?<>
            <input onChange={
                ({target: value}: React.ChangeEvent<HTMLInputElement>) => 
                  this.topicChangeNewNameInputOnChangeHandle(topics[i].name, value.value)
              } 
              type="text" placeholder="new name" />
            <button onClick={() => this.topicChangeConfirmButtonOnClickHandle(topics[i].name)} 
              type="button">change</button>
          </>:<></>
        }
        <button onClick={() => this.topicDeleteButtonClickHandle(topics[i].name)} 
          className={Styles.manipulationButton} title="delete">X</button>
        {
          selectedToChangeTopics.has(topics[i].name)?<>
            <span>{selectedToChangeTopics.get(topics[i].name)?.errorMessage}</span>
          </>:<></>
        }
      </li>
    }

    return <section>
      <h2 className="global_mainHeader">All own topics</h2>
      <ul className={Styles.ListUl}>{topics_}</ul>
    </section>;
  }
}

interface StateAddNewTopicsFormType {
  topicNameInput:string,
  statusMessage:string|null
}
class AddNewTopicsForm extends React.Component<unknown,StateAddNewTopicsFormType> {
  constructor(props: unknown) {
    super(props);

    this.state={
      topicNameInput:"",
      statusMessage:null
    };
  }

  topicNameInputOnChangeHandle = ({target: value}: React.ChangeEvent<HTMLInputElement>):void => {
    this.setState({topicNameInput: value.value});
  }
  addNewTopicButtonClickHandle = ():void => {
    const {topicNameInput}=this.state;

    if(!topicNameInput.match("^[a-zA-Z0-9_]{1,50}$")) {
      this.setState({
        statusMessage: "a topic name must contains of a-zA-Z0-9_ and has length from 1 to 50"
      });
      return;
    }

    addTopic(topicNameInput).then(res => {
      if(res===false) {
        this.setState({statusMessage: "a topic name is incorrect or is already used"});
        return;
      }

      this.setState({topicNameInput: "", statusMessage: `a new topic ${topicNameInput} added`});
      reloadListNeeded=true;
    });
  }

  render():React.ReactNode {
    const {topicNameInput, statusMessage}=this.state;

    return <section>
      <h2 className="global_mainHeader">Add a new topic</h2>
      <input onChange={this.topicNameInputOnChangeHandle} 
        type="text" placeholder="a topic name" value={topicNameInput}/><br />
      {statusMessage!==null?<><span>{statusMessage}</span><br /></>:<></>}
      <button onClick={this.addNewTopicButtonClickHandle}>add</button>
    </section>;
  }
}
