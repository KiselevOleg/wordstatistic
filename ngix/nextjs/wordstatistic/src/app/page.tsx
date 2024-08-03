'use client'

import React from "react";
import Styles from "./page.module.css";
import {getMostPopularWords, Word} from "./../api/globalstatisticAPI";

interface PropsPageType {

}
interface StatePageType {

}
export default class Page extends React.Component<PropsPageType, StatePageType> {
  constructor(props: PropsPageType) {
    super(props);

    this.state = {

    };
  }

  render():React.ReactNode {
    return <>
      <h2 className="global_mainHeader">List of the most popular words in all texts were ever loaded</h2>
      <ListOfMostPopularWords />
    </>;
  }
}

interface PropsListOfMostPopularWordsType {
  preload?: boolean
}
interface StateListOfMostPopularWordsType {
  words: Word[],
  wordsCount: number,
  preload: boolean
}
class ListOfMostPopularWords
  extends React.Component<PropsListOfMostPopularWordsType, StateListOfMostPopularWordsType> {
    constructor(props: PropsListOfMostPopularWordsType) {
      super(props);
      const {preload} = props;

      this.state = {
        words: [],
        wordsCount: 5,
        preload: preload??true
      };
    }

    countWordsInputRef:React.RefObject<HTMLInputElement> = React.createRef<HTMLInputElement>();

    componentDidMount(): void {
      const {preload} = this.state;
      if (!preload) return;

      let limitlast: number = +(
        localStorage.getItem("limitlast")??
        (():string => { 
          localStorage.setItem("limitlast", '5');
          return '5'; 
        })()
      );
      this.countWordsInputRef.current!.value=limitlast.toString();
      this.loadNewWordsListButtonClickHandle();
    }

    loadNewWordsListButtonClickHandle = ():void => {
      const limit:number = +this.countWordsInputRef.current!.value;
      localStorage.setItem("limitlast", limit.toString());
      if(limit<=0) return;
      getMostPopularWords(limit)
        .then(res => this.setState({words: res, wordsCount: limit}))
        .catch(e => e);
    }

    countWordsInputOnChangeHandle = ({target: value}: React.ChangeEvent<HTMLInputElement>):void => {
      let limit:number = +value.value;
      limit=Math.min(Math.max(limit,1),10000);
  
      this.setState({wordsCount: limit});
    }

    render():React.ReactNode {
      const {words, wordsCount} = this.state;
      console.log("render "+wordsCount);
      let w:React.ReactNode[]=[];
      for(let i:number=0;i<words.length;++i) {
        w[i]=<li key={words[i].name} className={Styles.wordLi}>
          <div className={Styles.word}>
            {words[i].name}: 
            <span className={Styles.wordCount}>{words[i].count}</span>
          </div>
        </li>;
      }

      return <>
        <input
          ref={this.countWordsInputRef}
          onChange={this.countWordsInputOnChangeHandle}
          type="number" value={wordsCount}
        />
        {/*<Input referenceValue={this.countWordsInputRef} startValue={wordsCount} />*/}
        <button onClick={this.loadNewWordsListButtonClickHandle}>load a new list</button>
        <ol>{w}</ol>
      </>;
    }
}

/*interface PropsInputType {
  referenceValue: React.RefObject<HTMLInputElement>,
  startValue: number
}
interface StateInputType {
  ref: React.RefObject<HTMLInputElement>,
  value: number
}
class Input extends React.Component<PropsInputType, StateInputType> {
  constructor(props: PropsInputType) {
    super(props);
    const {referenceValue, startValue} = props;

    this.state = {
      ref: referenceValue,
      value: startValue
    };
    console.log("render in creation "+startValue);
  }

  countWordsInputOnChangeHandle = ({target: value}: React.ChangeEvent<HTMLInputElement>):void => {
    let limit:number = +value.value;
    limit=Math.min(Math.max(limit,1),10000);

    this.setState({value: limit});
  }

  render():React.ReactNode {
    const {value, ref} = this.state;
    console.log("render in "+value);

    return <input
      ref={ref}
      onChange={this.countWordsInputOnChangeHandle}
      type="number" value={value}
    />;
  }
}*/
