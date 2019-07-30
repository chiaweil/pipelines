from setuptools import setup
from setuptools import find_packages

setup(
    name='pipes',
    version='0.1',
    packages=find_packages(),
    install_requires=[
        'requests>=2.22.0',
        'numpy<=1.16.4',  # For compatibility with python 2
        'pyarrow==0.13.0',
        'requests-toolbelt>=0.9.1',
        'pandas<=0.24.2'  # For compatibility with python 2
    ],
    extras_require={
        'tests': ['pyjnius', 'pytest', 'pytest-pep8', 'pytest-cov', 'mock', 'pydatavec']
    },
    include_package_data=True,
    license='Apache',
    description='pipes: Deploy your machine learning experiments with Skymind Pipelines',
    long_description='pipes: Deploy your machine learning experiments with Skymind Pipelines',
    author='Max Pumperla',
    author_email='max@skymind.io',
    url='https://github.com/SkymindIO/pipelines',
    classifiers=[
        'Development Status :: 3 - Alpha',
        'Intended Audience :: Developers',
        'Environment :: Console',
        'License :: OSI Approved :: Apache Software License',
        'Operating System :: OS Independent',
        'Programming Language :: Python',
        'Programming Language :: Python :: 2',
        'Programming Language :: Python :: 3'
    ]
)
