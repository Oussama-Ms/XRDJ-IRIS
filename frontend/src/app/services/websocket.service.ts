import { Injectable } from '@angular/core';
import { Client } from '@stomp/stompjs';
import { Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private client: Client;
  private alertSubject = new Subject<string>();

  public alerts$ = this.alertSubject.asObservable();

  constructor() {
    this.client = new Client({
      brokerURL: 'ws://localhost:8080/ws',
      debug: (str) => {
        // console.log(new Date(), str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000
    });

    this.client.onConnect = (frame) => {
      this.client.subscribe('/topic/alerts', (message) => {
        if (message.body) {
          this.alertSubject.next(message.body);
        }
      });
    };

    this.client.onStompError = (frame) => {
      console.error('Broker reported error: ' + frame.headers['message']);
      console.error('Additional details: ' + frame.body);
    };

    this.client.activate();
  }
}
