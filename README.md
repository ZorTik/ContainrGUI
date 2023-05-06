<div align="center">
<!-- Image -->
<br>
<img src="https://user-images.githubusercontent.com/67344817/221914079-d2b0aa8d-4895-4b11-9d7a-7f338e56cb5f.png" width=150 height=150>

# Containr GUI
An advanced GUI solution. Build fast, responsible, animated menus.<br>Let's imagine almost unlimited GUI possibilities.<br>

![Badge](https://img.shields.io/jitpack/version/com.github.ZorTik/ContainrGUI?style=for-the-badge) ![Badge](https://img.shields.io/github/license/ZorTik/ContainrGUI?style=for-the-badge)
</div>

## About
Creating a static GUI (Graphical User Interface) can be a tedious and time-consuming task, especially when working with complex environments like Minecraft. That's where the Containr library comes in. **Reactive**, **modular** and highly **extendable**. These are the words that describe Containr well!

Hello World:
```java
GUI gui = Component.gui()
    .title("GUI Title")
    .rows(3)
    .prepare((g) -> {
        g.setContainer(1, Component.staticContainer()
	    .size(4, 1)
	    .init(c -> {
	    	c.fillElement(Component.element()
                    .click(info -> {
                        info.close();
                        info.getPlayer().sendMessage("§7Hello World!");
                    })
                    .item(Items.create(Material.REDSTONE_BLOCK, "§aHello World Message"))
                    .build());
	    })
	    .build());
    })
    .build();
    
gui.open(player);
```
[More on new Wiki](https://github.com/ZorTik/ContainrGUI/wiki)

## Installation
You can add Containr to your build path using Maven or Gradle. ContainrGUI is **not a Minecraft plugin**! This means that you can use it's code directly in your project by shading it into your build path.

<details><summary>Gradle</summary>

Add this project to your build path using Gradle with JitPack as represented below.
```gradle
repositories {
	maven { url = 'https://jitpack.io' }
}
```
```gradle
dependencies {
	implementation 'com.github.ZorTik:ContainrGUI:0.6'
}
```
</details>

<details><summary>Maven</summary>

You can also use Maven with JitPack as seen below.
```xml
<repositories>
	<repository>
		<id>jitpack.io</id>
		<url>https://jitpack.io</url>
	</repository>
</repositories>
```
```xml
<dependency>
	<groupId>com.github.ZorTik</groupId>
		<artifactId>ContainrGUI</artifactId>
	<version>0.6</version>
</dependency>
```
</details>
