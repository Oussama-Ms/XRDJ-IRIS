import { Component } from '@angular/core';
import { ChatService } from '../../services/chat.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface Message {
  text: string;
  sender: 'user' | 'agent';
}

@Component({
  selector: 'app-chat-widget',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chat-widget.component.html',
  styleUrls: ['./chat-widget.component.css']
})
export class ChatWidgetComponent {
  isOpen = false;
  userInput = '';
  isLoading = false;
  messages: Message[] = [
    { text: 'Hello! I am your AI Data Assistant. How can I help you today?', sender: 'agent' }
  ];

  premadeQuestions = [
    'How many transactions were done today?',
    'How many transactions were rejected today?',
    'How many transactions were treated correctly?',
    "Give me a full breakdown of today's metrics.",
    'Were there more CRE or EC rejections today?'
  ];

  usedQuestions = new Set<string>();

  get availableQuestions() {
    return this.premadeQuestions.filter((q) => !this.usedQuestions.has(q));
  }

  constructor(private chatService: ChatService) {}

  formatMessage(text: string): string {
    if (!text) return '';
    let formatted = text.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
    // Force a space between any letter and a number to fix hallucinated missing spaces
    formatted = formatted.replace(/([a-zA-Z])(\d)/g, '$1 $2');
    // Replace periods with line breaks to ensure spacious formatting
    formatted = formatted.replace(/\.\s*/g, '.<br><br>');
    // Clean up any stray plus signs or asterisks the LLM might hallucinate
    formatted = formatted.replace(/[+*]/g, '');
    return formatted;
  }

  toggleChat() {
    this.isOpen = !this.isOpen;
  }

  askPremade(question: string) {
    this.usedQuestions.add(question);
    this.userInput = question;
    this.sendMessage();
  }

  async sendMessage() {
    if (!this.userInput.trim() || this.isLoading) return;

    const prompt = this.userInput.trim();
    this.messages.push({ text: prompt, sender: 'user' });
    this.userInput = '';

    const agentMessage: Message = { text: '', sender: 'agent' };
    this.messages.push(agentMessage);

    this.isLoading = true;

    try {
      await this.chatService.sendMessageStream(prompt, (chunk) => {
        this.isLoading = false;
        agentMessage.text += chunk;
      });
    } catch (err) {
      console.error('Chat API Error:', err);
      if (!agentMessage.text) {
        agentMessage.text = 'Error connecting to the AI agent. Please try again.';
      }
      // If we already have text, we just swallow the error because it's usually a harmless stream-close reset from the backend
    } finally {
      this.isLoading = false;
    }
  }
}
