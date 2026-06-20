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


@app.route('/error1')
def error_undefined_variable():
    if request.method == 'POST':
        result = mystery_variable
    return render_template('products.html')


@app.route('/error2')
def error_undefined_function():
    if request.method == 'POST':
        ghost_function(products_data)
    return render_template('products.html')


@app.route('/error3/<int:product_id>')
def error_duplicate_param(product_id, product_id=None):
    return render_template('products.html')


@app.route('/error4')
def error_wrong_arg_count():
    if request.method == 'POST':
        products(products_data)
    return render_template('products.html')


@app.route('/error5')
def error_unreachable_code():
    if request.method == 'POST':
        return redirect(url_for('products'))
        leftover_call(products_data)
    return render_template('products.html')


@app.route('/error6')
def error_invalid_type():
    if products_data == 'empty':
        return render_template('products.html')
    return render_template('products.html')


if __name__ == '__main__':
    app.run(debug=True)