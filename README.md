***

# College Productivity App
### Capstone | Team 6'7" - CSIT227

## MEMBERS:
- Miscreola, Hertz Lenin
- Ondon, Veneerick Joel
- Dy, Rykiel Von David
- Dolores, Mike Juneil
- Gidayawan, Jerry

## Features
The application is divided into three main tabs to handle different aspects of student productivity.

- **Projects Tab**: Manage tasks and project completion.
- **Pomodoro Tab**: Focus timer with background music.
- **CIT GWA Calculator Tab**: Grade tracking and calculation.

---

### 1. Projects Tab
This tab is split into two panels: the **Project List** (Left) and the **Task Manager** (Right).

#### **Project List Panel (Left)**
- **Project Management**:
  - **Add Project**: Create a new project with a unique name.
  - **Rename Project**: Update the name of an existing project.
  - **Remove Project**: Permanently delete a project and its associated data file.
- **Selection**: Clicking a project loads its tasks into the right panel.

#### **Task Manager Panel (Right)**
- **Progress Bar**: Located at the top. Visualizes the completion percentage of the current project based on finished vs. total tasks.
- **Sorting Options**: A dropdown menu allows you to sort tasks by:
  - **Deadline** (Default)
  - **Difficulty** (Low to High)
  - **Name** (A-Z)
- **Task Categorization**:
  - **Unfinished Tab**: Active tasks waiting to be completed.
  - **Finished Tab**: Completed tasks are moved here automatically to keep the workspace clean.
- **Task Actions**:
  - **Add Task**: Opens a dialog requesting:
    - **Task Name**
    - **Deadline** (Date picker included)
    - **Difficulty** (0 to 3 stars)
  - **Edit Task** (Pencil Icon): Modify the name, deadline, or difficulty of an existing task.
  - **Complete/Undo** (Check/Undo Icon): Toggles the task state. Completed tasks move to the "Finished" tab; undoing them moves them back to "Unfinished".
  - **Delete Task** (Trash Icon): Permanently removes the task.

---

### 2. Pomodoro Tab
A focus timer designed to help students manage study sessions using the Pomodoro technique.

- **Customizable Timer Settings**: Input fields to define durations for:
  - **Work Session** (Default: 25 min)
  - **Short Break** (Default: 5 min)
  - **Long Break** (Default: 15 min)
  - **Cycles** (Number of work sessions before a long break).
- **Background Music (BGM)**:
  - Includes preset tracks (Mondstadt, Liyue, Inazuma, Sumeru, Fontaine).
  - **Custom Music**: Allows the user to select their own `.mp3` file from their computer.
  - **Auto-Play**: Music plays automatically during Work sessions and stops during breaks or when paused.
- **State Tracking**: Displays the current status (WORK, SHORT BREAK, LONG BREAK, IDLE, PAUSED) and the current cycle count (e.g., Cycle: 1/4).

---

### 3. CIT GWA Calculator Tab
A tool specifically designed to calculate the General Weighted Average based on units and grades in the way the Cebu Institute of Technology does it.

- **Subject Management**:
  - **Add Subject**: Input the Subject Name, Units (e.g., 3.0), and Grade (1.0 - 5.0).
  - **Edit Subject**: Modify details of selected subjects.
  - **Remove Subject**: Delete a subject from the list.
- **Calculation Display**:
  - Calculates the final GWA rounded to two decimal places.
  - Displays the result

### FILE HANDLING
All progress is automatically saved locally.



### GUI Draft for PROJECT TRACKER:
<img width="792" height="508" alt="image" src="https://github.com/user-attachments/assets/7e6319fa-44ff-4073-8afd-db38ef11281f" />




