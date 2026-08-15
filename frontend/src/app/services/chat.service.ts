import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ChatRequest {
  prompt: string;
  chatId?: string;
}

export interface ChatResponse {
  response: string;
}

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private apiUrl = 'http://localhost:8080/api/chat';
  private chatId = crypto.randomUUID();

  constructor(private http: HttpClient) {}

  resetChatId() {
    this.chatId = crypto.randomUUID();
  }

  sendMessage(prompt: string): Observable<ChatResponse> {
    return this.http.post<ChatResponse>(this.apiUrl, { prompt, chatId: this.chatId });
  }

  async sendMessageStream(prompt: string, onChunk: (chunk: string) => void): Promise<void> {
    const token = localStorage.getItem('token');
    const headers: Record<string, string> = {
      'Content-Type': 'application/json'
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${this.apiUrl}/stream`, {
      method: 'POST',
      headers,
      body: JSON.stringify({ prompt, chatId: this.chatId })
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    if (!response.body) throw new Error('ReadableStream not supported');

    const reader = response.body.getReader();
    const decoder = new TextDecoder('utf-8');

    let buffer = '';
    while (true) {
      const { done, value } = await reader.read();
      if (done) break;

      buffer += decoder.decode(value, { stream: true });

      let doubleNewlineIndex;
      while ((doubleNewlineIndex = buffer.indexOf('\n\n')) !== -1) {
        const eventData = buffer.substring(0, doubleNewlineIndex);
        buffer = buffer.substring(doubleNewlineIndex + 2);

        const lines = eventData.split('\n');
        const dataLines = [];
        for (const line of lines) {
          if (line.startsWith('data:')) {
            let text = line.substring(5);
            // Do NOT strip the leading space, as the tokenizer relies on it for word spacing
            dataLines.push(text);
          }
        }

        if (dataLines.length > 0) {
          onChunk(dataLines.join('\n'));
        }
      }
    }
  }
}
