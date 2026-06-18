# Play Store Graphic Assets

Generated assets for the Journey Play Store listing. Upload these in Play Console → Store presence → Main store listing.

## Upload these

| File | Play Console slot | Spec | Status |
|---|---|---|---|
| `feature_graphic.png` | Feature graphic | 1024 × 500 PNG | ✅ Ready |
| `screenshot_1.png` … `screenshot_5.png` | Phone screenshots | 1242 × 2208 PNG (ratio 1.78:1, within Play's 2:1 max) | ✅ Ready |
| `app icon (512×512)` | App icon | 512 × 512 PNG | ⬜ Still needed — export from the launcher icon |

### Screenshot captions
1. Build a daily writing habit (Home)
2. See your streaks and habits (Analytics)
3. Distraction-free journaling (Add entry)
4. Every entry, beautifully kept (View entry)
5. Private, secure, and yours (Settings)

## Source files
`raw_*.png` are the unframed device captures (1080 × 2424) used to build the
final screenshots — kept for regeneration. The sample journal data shown was
injected only into the emulator for capture; it is not part of the app.

## Regenerating
The phone screenshots are composited from the `raw_*.png` captures by
`tools/build_store_assets.py` (Pillow), using the app's own Manrope font
(`app/src/main/res/font/`) on an indigo→violet brand gradient:

```bash
python3 tools/build_store_assets.py            # rewrites screenshot_1..5.png
python3 tools/build_store_assets.py --out /tmp/preview   # preview without overwriting
```

To re-shoot a raw capture, install a debug build, populate sample data, and run
`adb exec-out screencap -p > docs/store-assets/raw_<screen>.png`, then rebuild.
The `feature_graphic.png` (1024×500) is a separate composition and is not
produced by this script.
