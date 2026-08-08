"""
Generate solid tabbar icons as RGB PNGs with white background.
Uses bold, simple shapes that render reliably everywhere.
"""
from PIL import Image, ImageDraw
import os, sys

SIZE = 81
WHITE = (255, 255, 255)
GRAY = (153, 153, 153)
ORANGE = (255, 80, 0)

OUT_DIRS = [
    os.path.join(os.path.dirname(__file__), '..',
                 'shop-frontend', 'packages', 'mobile', '__static__', 'static', 'tabbar'),
    os.path.join(os.path.dirname(__file__), '..',
                 'shop-frontend', 'packages', 'mobile', 'static', 'tabbar'),
    os.path.join(os.path.dirname(__file__), '..',
                 'shop-frontend', 'packages', 'mobile', 'src', 'static', 'tabbar'),
]

def solid_circle(draw, cx, cy, r, color):
    draw.ellipse([cx - r, cy - r, cx + r, cy + r], fill=color)

def draw_home(draw, color):
    m = 6
    cx = SIZE // 2
    # roof
    draw.polygon([(cx, 4), (m, 26), (SIZE - m, 26)], fill=color)
    # chimney
    draw.rectangle([cx + 14, 6, cx + 26, 24], fill=color)
    # walls
    draw.rectangle([m + 4, 24, SIZE - m - 4, SIZE - m], fill=color)
    # door cutout
    dw, dh = 16, 22
    draw.rectangle([cx - dw // 2, SIZE - m - dh - 2, cx + dw // 2, SIZE - m], fill=WHITE)

def draw_category(draw, color):
    m = 10
    gap = 6
    w = (SIZE - 2 * m - gap) // 2
    h = (SIZE - 2 * m - gap) // 2
    for r in range(2):
        for c in range(2):
            x = m + c * (w + gap)
            y = m + r * (h + gap)
            draw.rounded_rectangle([x, y, x + w, y + h], radius=8, fill=color)

def draw_cart(draw, color):
    m = 6
    # cart body
    draw.rounded_rectangle([m, 26, SIZE - m, SIZE - 10], radius=10, fill=color)
    # wheels
    wr = 7
    draw.ellipse([m + 4, SIZE - 18, m + 4 + wr * 2, SIZE - 18 + wr * 2], fill=WHITE)
    draw.ellipse([SIZE - m - 22, SIZE - 18, SIZE - m - 22 + wr * 2, SIZE - 18 + wr * 2], fill=WHITE)
    # handle arc
    hx = SIZE // 2
    draw.arc([hx - 13, 4, hx + 13, 30], start=180, end=0, fill=color, width=6)
    # handle bar
    draw.rectangle([hx - 12, 2, hx + 12, 10], fill=color)

def draw_user(draw, color):
    m = 8
    hx = SIZE // 2
    # head
    solid_circle(draw, hx, 18, 14, color)
    # body
    draw.rounded_rectangle([m + 10, 30, SIZE - m - 10, SIZE - m], radius=14, fill=color)

MAKERS = {'home': draw_home, 'category': draw_category, 'cart': draw_cart, 'user': draw_user}

def make_one(name, color):
    img = Image.new('RGB', (SIZE, SIZE), WHITE)
    draw = ImageDraw.Draw(img)
    for key, fn in MAKERS.items():
        if key in name:
            fn(draw, color)
            break
    for out_dir in OUT_DIRS:
        path = os.path.join(out_dir, name + '.png')
        os.makedirs(out_dir, exist_ok=True)
        img.save(path)
        print(f'  {path}')
    return True

if __name__ == '__main__':
    print('Generating icons...')
    for prefix, active in [('home', False), ('home', True),
                            ('category', False), ('category', True),
                            ('cart', False), ('cart', True),
                            ('user', False), ('user', True)]:
        clr = ORANGE if active else GRAY
        nm = f'{prefix}-active' if active else prefix
        make_one(nm, clr)
    print('Done!')
