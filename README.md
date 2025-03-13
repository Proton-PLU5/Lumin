# Lumin

<p align="center">
    <img src=https://github.com/Proton-PLU5/Lumin/blob/main/src/main/resources/me/protonplus/lumin/images/lumin.png?raw=true>
</p>

A Personalized Desktop Assistant developed to help users use their desktops more efficently.

## Overview

Lumin is a personalized desktop assistant designed to help you with various tasks such as getting weather updates, creating sticky notes and answering questions. It leverages voice recognition technologies and integrates with various APIs to provide a seamless user experience.

## Features
- **Large Language Model**: Uses Google's Gemini model to provide accurate conversations that feel personalized. Can answer various questions and have conversations with the user easily.
- **Voice Recognition**: Uses Picovoice's Porcupine and Cheetah for wake word detection and speech-to-text processing.
- **Intent Recognition**: Uses Wit.AI to provide intent recognition allowing users to execute tasks by simply telling Lumin to do so.
- **Weather Updates**: Fetches and displays weather information based on your location in a elegant user interface.
- **Sticky Notes**: Provides users with the ability to make customizable sticky notes where the user can add images and change the theme of the note.

## Getting Started

### Prerequisites

- Java 17
- Gradle
- Internet connection for API access

### Installation

1. Clone the repository:
    ```sh
    git clone https://github.com/Proton-PLU5/Lumin.git
    cd Lumin
    ```

2. Set up environment variables:
   
   > *As lumin is still in beta stages, we cannot offer API keys to our users, therefore, the API keys must be obtained by the user.*
    - `PICO_API_TOKEN`
    - `WIT_API_ID`
    - `WIT_ACCESS_TOKEN`
    - `WEATHER_API_TOKEN`
    - `MAXMIND_LICENSE_KEY`
    - `MAXMIND_ACCOUNT_ID`

3. Build the project:
   > *Collate all the required dependencies in order for Lumin to function.*
    ```sh
    ./gradlew build
    ```

4. Create the runnable:
   > *Create a executable image using runtime for execution, this will be generated in the builds folder under a folder called image.*
    ```sh
    ./gradlew runtime
    ```

### Future Plans
- Implement reminder capabilities with access to Calander to allow users to plan and create events.
- Autocomplete feature in Documents to help users create and write documents
- Screen image recognition to provide enhanced assistance capabilities.
