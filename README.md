# Loombook

A Minecraft Fabric mod that enhances the loom experience by providing a convenient side panel for saving, managing, and crafting banner patterns.

## Features

### Banner Management
- **Save Banners**: Save banner designs from the loom for later use
  - Automatically saves from the input slot if no dyes or patterns are applied
  - Saves from the output slot if patterns have been applied
- **Rename Banners**: Edit the names of your saved banner designs
- **Delete Banners**: Remove banners you no longer need
- **View Saved Banners**: Browse all your saved banner designs in a convenient side panel

### Multi-Select Functionality
- **Single Click**: Select a single banner
- **Ctrl+Click**: Toggle individual banner selection for multi-select
- **Shift+Click**: Select a range of banners between the last clicked and current banner
- **Visual Feedback**: Selected banners are highlighted in blue with a border

### Import/Export
- **Export Single Banner**: Export a single banner as JSON to clipboard
- **Export Multiple Banners**: Export multiple selected banners as a JSON array
- **Import Banners**: Import banner designs from JSON format
  - Supports both single banner JSON objects and JSON arrays
  - Automatically handles ID conflicts by generating new IDs

### Crafting
- **Single Craft**: Click the Craft button to craft a selected banner
- **Multi-Craft**: Select multiple banners and craft them sequentially
- **Auto-Craft**: The mod automatically handles the crafting process

### Materials Display
- **Right-Click Materials**: Right-click any saved banner to view required materials
- **Item Icons**: See actual item icons for all required materials
- **Dye Quantities**: View how many of each dye color is needed
- **Pattern Items**: See which banner pattern items are required
- **Scrollable List**: Scroll through the materials list if there are many items

## Installation

1. Download the latest release of Loombook
2. Place the JAR file in your Minecraft `mods` folder
3. Ensure you have Fabric Loader and Fabric API installed
4. Launch Minecraft with the Fabric profile

## Usage

### Accessing the Loom Panel
When you open a loom, a side panel will appear on the right side showing your saved banners.

### Saving Banners
1. Create a banner design in the loom
2. Click the "+ Save" button in the side panel
3. The banner will be saved with an auto-generated name or your custom name

### Managing Banners
- **Rename**: Click the "E" button next to a banner to rename it
- **Delete**: Click the "X" button next to a banner to delete it
- **Select**: Click a banner to select it (for crafting or exporting)

### Multi-Select Operations
1. Click a banner to select it
2. Hold Ctrl and click other banners to add them to the selection
3. Hold Shift and click to select a range of banners
4. Use the Craft or Export buttons to perform actions on all selected banners

### Exporting Banners
1. Select one or more banners
2. Click the "Export" button
3. The banner(s) will be copied to your clipboard as JSON
4. Share the JSON with others or save it for later

### Importing Banners
1. Click the "Import" button
2. Paste the banner JSON into the text editor
3. Click "OK" to import the banner(s)

### Viewing Materials
1. Right-click any saved banner in the panel
2. A materials screen will open showing:
   - The base banner color
   - All dyes needed with quantities
   - All banner pattern items required
3. Click "Close" to return to the loom

### Crafting Banners
1. Select one or more banners from the panel
2. Click the "Craft" button at the bottom
3. The mod will automatically craft the banner(s) using the loom

## Configuration

Banners are automatically saved to:
```
.minecraft/config/loombook/banners/
```

Each banner is stored as a separate JSON file with a unique ID.

## Technical Details

### Architecture
- **LoomSidePanel**: Main UI component for the side panel
- **BannerStorage**: Handles saving and loading banner data
- **BannerPreviewRenderer**: Renders banner previews with patterns
- **BannerMaterialsScreen**: Displays required materials for a banner
- **ImportBannerScreen**: Provides UI for importing banners
- **AutoCraftStateMachine**: Manages the automatic crafting process

### Data Format
Banners are stored in JSON format with the following structure:
```json
{
  "id": "unique-uuid",
  "name": "Banner Name",
  "baseColor": "white",
  "layers": [
    {
      "patternId": "minecraft:globe",
      "color": "red"
    }
  ],
  "createdAt": 1234567890
}
```

## Compatibility

- Minecraft 1.20+
- Fabric Loader 0.14+
- Fabric API

## Known Limitations

- The mod only works with Fabric (not Forge)
- Banner patterns are limited to those available in your Minecraft version
- Multi-crafting requires the loom to be accessible for each banner

## Troubleshooting

### Banners not saving
- Ensure the loom has a banner in the input slot
- Check that the `config/loombook/banners/` directory exists and is writable

### Import not working
- Verify the JSON format is correct
- Ensure you're using valid banner pattern IDs
- Check the game log for error messages

### Crafting fails
- Ensure you have enough dyes and pattern items in your inventory
- Check that the loom is still accessible
- Verify the banner design is valid

## Contributing

Contributions are welcome! Please feel free to submit issues or pull requests.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Credits

Developed for the Minecraft community to enhance the loom crafting experience.
