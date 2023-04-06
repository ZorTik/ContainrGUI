<div align="center">
<!-- Image -->
<br>
<img src="https://user-images.githubusercontent.com/67344817/221914079-d2b0aa8d-4895-4b11-9d7a-7f338e56cb5f.png" width=150 height=150>

# Containr GUI
An advanced GUI solution. Build fast, responsible, animated menus.<br>Let's imagine almost unlimited GUI possibilities.<br>

![Badge](https://img.shields.io/jitpack/version/com.github.ZorTik/ContainrGUI?style=for-the-badge) ![Badge](https://img.shields.io/github/license/ZorTik/ContainrGUI?style=for-the-badge)
</div>

## About
Creating a static GUI (Graphical User Interface) can be a tedious and time-consuming task, especially when working with complex environments like Minecraft. That's where the Containr library comes in. With its intuitive API and user-friendly approach, Containr simplifies the process of GUI creation in Minecraft, allowing developers to focus on the functionality of their mods rather than the intricacies of GUI design. **Reactive**, **modular** and highly **extendable**. These are the words that describe Containr well!

## Installation
You can add Containr to your build path using Maven or Gradle. ContainrGUI is **not a Minecraft plugin**! This means that you can use it's code directly in your project by shading it into your build path.

<details><summary>Gradle</summary>

Add this project to your build path using Gradle with JitPack as represented below.
```
repositories {
	maven { url = 'https://jitpack.io' }
}
```
```
dependencies {
	implementation 'com.github.ZorTik:ContainrGUI:Tag'
}
```
</details>

<details><summary>Maven</summary>

You can also use Maven with JitPack as seen below.
```
<repositories>
	<repository>
		<id>jitpack.io</id>
		<url>https://jitpack.io</url>
	</repository>
</repositories>
```
```
<dependency>
	<groupId>com.github.ZorTik</groupId>
		<artifactId>ContainrGUI</artifactId>
	<version>Version</version>
</dependency>
```
</details>

[More on new Wiki](https://github.com/ZorTik/ContainrGUI/wiki)
