"use client";

import React from "react";

import {refreshTokens, validTokens} from "./../api/userAPI";

export default class RefreshTokens extends React.Component<unknown, unknown> {
  constructor(props: unknown) {
    super(props);

    this.state= {

    };
  }

  componentDidMount(): void {
    if(!validTokens()) refreshTokens();
    setInterval(() => {refreshTokens()}, 50000);
  }

  render():React.ReactNode {
    return <></>;
  }
}
