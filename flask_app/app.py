from flask import Flask, render_template, request, redirect, url_for
import os
from werkzeug.utils import secure_filename

app = Flask(__name__)

UPLOAD_FOLDER = os.path.join('static', 'uploads')
os.makedirs(UPLOAD_FOLDER, exist_ok=True)
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

products_data = []


@app.route('/')
def products():
    return render_template('products.html', products=products_data)


@app.route('/add', methods=['GET', 'POST'])
def add_product():
    if request.method == 'POST':
        name = request.form.get('name')
        price = request.form.get('price')
        details = request.form.get('details')
        image = request.files.get('image')
        filename = None
        if image:
            if image.filename != '':
                filename = secure_filename(image.filename)
                image_path = os.path.join(app.config['UPLOAD_FOLDER'], filename)
                image.save(image_path)
        new_product = dict()
        new_product['id'] = len(products_data)
        new_product['name'] = name
        new_product['price'] = price
        new_product['details'] = details
        new_product['image'] = filename
        products_data.append(new_product)
        return redirect(url_for('products'))
    return render_template('add_product.html')


@app.route('/product/<int:product_id>')
def product_details(product_id):
    product = None
    for p in products_data:
        if p['id'] == product_id:
            product = p
    if product == None:
        return "Product not found", 404
    return render_template('product_details.html', product=product)


@app.route('/delete/<int:product_id>', methods=['GET', 'POST'])
def delete_product(product_id):
    product = None
    for p in products_data:
        if p['id'] == product_id:
            product = p
    if product == None:
        return "Product not found", 404
    if request.method == 'POST':
        products_data.remove(product)
        return redirect(url_for('products'))
    return render_template('delete_product.html', product=product)


if __name__ == '__main__':
    app.run(debug=True)