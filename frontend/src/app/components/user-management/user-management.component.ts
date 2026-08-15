import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { UserService, UserDto } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './user-management.component.html',
  styleUrls: ['./user-management.component.css']
})
export class UserManagementComponent implements OnInit {
  users: UserDto[] = [];
  showModal: boolean = false;
  isEditMode: boolean = false;

  currentUser: any = {
    id: null,
    username: '',
    password: '',
    role: 'ROLE_USER'
  };

  currentLoggedInUsername: string = '';

  constructor(
    private userService: UserService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.currentLoggedInUsername = this.authService.getUsername() || '';
    this.loadUsers();
  }

  loadUsers(): void {
    this.userService.getAllUsers().subscribe({
      next: (data) => {
        this.users = data;
      },
      error: (err) => {
        console.error('Error loading users', err);
      }
    });
  }

  openAddModal(): void {
    this.isEditMode = false;
    this.currentUser = { id: null, username: '', password: '', role: 'ROLE_USER' };
    this.showModal = true;
  }

  openEditModal(user: UserDto): void {
    this.isEditMode = true;
    this.currentUser = {
      id: user.id,
      username: user.username,
      password: '', // Blank password so it doesn't get overwritten unless they type a new one
      role: user.role
    };
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
  }

  saveUser(): void {
    if (!this.currentUser.username || (!this.isEditMode && !this.currentUser.password)) {
      alert('Username and password are required!');
      return;
    }

    if (this.isEditMode) {
      this.userService.updateUser(this.currentUser.id, this.currentUser).subscribe({
        next: (res) => {
          this.closeModal();
          this.loadUsers();
        },
        error: (err) => {
          alert('Error updating user: ' + (err.error || err.message));
        }
      });
    } else {
      this.userService.createUser(this.currentUser).subscribe({
        next: (res) => {
          this.closeModal();
          this.loadUsers();
        },
        error: (err) => {
          alert('Error creating user: ' + (err.error || err.message));
        }
      });
    }
  }

  deleteUser(user: UserDto): void {
    if (user.username === this.currentLoggedInUsername) {
      alert('You cannot delete your own admin account!');
      return;
    }

    if (confirm(`Are you sure you want to delete user ${user.username}?`)) {
      this.userService.deleteUser(user.id).subscribe({
        next: (res) => {
          this.loadUsers();
        },
        error: (err) => {
          alert('Error deleting user: ' + (err.error || err.message));
        }
      });
    }
  }
}
